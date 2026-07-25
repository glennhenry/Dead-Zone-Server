package encore.datastore

import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.account.FieldEmail
import encore.account.FieldPlayerId
import encore.account.FieldUsername
import game.mongo.collection.Inventory
import game.mongo.collection.NeighborHistory
import encore.fancam.Fancam
import encore.fancam.Tags
import encore.utils.support.asUnit
import game.mongo.MongoCollections
import game.mongo.collection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import org.bson.conversions.Bson
import kotlin.time.measureTime

/** `dbId`*/
val ServerObjectsDbId = ServerObjects::dbId.name

/** Mongo filters for `dbId` equals [ServerObjectsId]*/
val ServerObjectsFilter: Bson = Filters.eq(ServerObjectsDbId, ServerObjectsId)

/**
 * Implementation of [DataStore] with Kotlin MongoDB coroutine driver.
 */
class MongoDataStore(db: MongoDatabase, collections: MongoCollections) : DataStore {
    private val accounts = db.getCollection<PlayerAccount>(collections.playerAccount)

    private val playerObjects = db.getCollection<PlayerObjects>(collections.playerObjects)
    private val neighbors = db.getCollection<NeighborHistory>(collections.neighborHistory)
    private val inventories = db.getCollection<Inventory>(collections.inventory)

    private val playerServerObjects = db.getCollection<PlayerServerObjects>(collections.playerServerObjects)
    private val serverObjects = db.getCollection<ServerObjects>(collections.serverObjects)

    private val initJob = CoroutineScope(Dispatchers.IO).async { setupCollections() }

    override suspend fun awaitInit() {
        Fancam.info(Tags.Datastore) { "Waiting for MongoDB initialization..." }
        val elapsed = measureTime {
            initJob.await()
        }
        Fancam.info(Tags.Datastore) { "MongoDB initialized in ${elapsed.inWholeMilliseconds}ms" }
    }

    private suspend fun setupCollections() {
        try {
            val count = accounts.estimatedDocumentCount()
            Fancam.info(Tags.Datastore) { "MongoDB contains $count accounts" }
            prepareServerObjects()
            setupIndexes()
            setupUniqueConstraints()
        } catch (e: Exception) {
            Fancam.error(e, Tags.Datastore) { "MongoDB scandal during initialization" }
        }
    }

    private suspend fun setupIndexes() {
        serverObjects.createIndex(Indexes.text())
        Fancam.info(Tags.Datastore) { "Mongo index set up" }
    }

    private suspend fun setupUniqueConstraints() {
        // account.username unique
        accounts.createIndex(
            Indexes.ascending(FieldUsername), IndexOptions().unique(true)
        )
    }

    private suspend fun prepareServerObjects() {
        when (val count = serverObjects.estimatedDocumentCount()) {
            0L -> {
                serverObjects.insertOne(ServerObjects())
            }

            1L -> return

            else -> {
                Fancam.warn(Tags.Datastore) { "Detected multiple server object document count=$count" }
            }
        }
    }

    override suspend fun accountExists(playerId: PlayerId): Boolean {
        return accounts.find(Filters.eq(FieldPlayerId, playerId)).firstOrNull() != null
    }

    override suspend fun playerObjects(playerId: PlayerId): PlayerObjects? {
        return playerObjects.find(Filters.eq(FieldPlayerId, playerId)).firstOrNull()
    }

    override suspend fun loadObjects(playerId: PlayerId): Triple<PlayerObjects, NeighborHistory, Inventory>? {
        return try {
            val a = playerObjects.find(Filters.eq(FieldPlayerId, playerId)).firstOrNull()
            val b = neighbors.find(Filters.eq(FieldPlayerId, playerId)).firstOrNull()
            val c = inventories.find(Filters.eq(FieldPlayerId, playerId)).firstOrNull()
            return if (a != null && b != null && c != null) {
                Triple(a, b, c)
            } else {
                Fancam.error(tag = Tags.Datastore) { "One of the field is null: (a.null=${a == null}, b.null=${b == null}, c.null=${c == null})" }
                null
            }
        } catch (e: Exception) {
            Fancam.error(e, Tags.Datastore) { "Mongo error on load objects" }
            null
        }
    }

    override suspend fun insert(
        account: PlayerAccount,
        playerObjects: PlayerObjects,
        neighborHistory: NeighborHistory,
        inventory: Inventory,
        playerServerObjects: PlayerServerObjects
    ): Result<Unit> {
        return runMongoCatching {
            accounts.insertOne(account)
            this.playerObjects.insertOne(playerObjects)
            this.playerServerObjects.insertOne(playerServerObjects)
            this.neighbors.insertOne(neighborHistory)
            this.inventories.insertOne(inventory).asUnit()
        }
    }

    override suspend fun <T> updatePlayerObjectsField(
        playerId: String, path: String, value: T
    ) {
        val filter = Filters.eq(FieldPlayerId, playerId)
        val update = Updates.set(path, value)
        playerObjects.updateOne(filter, update)
    }

    override suspend fun delete(playerId: PlayerId): Result<Unit> {
        return runMongoCatching {
            accounts.deleteOne(Filters.eq(FieldPlayerId, playerId))
                .throwIfNothingDeleted("accounts")
            playerObjects.deleteOne(Filters.eq(FieldPlayerId, playerId))
                .throwIfNothingDeleted("playerObjects")
            playerServerObjects.deleteOne(Filters.eq(FieldPlayerId, playerId))
                .throwIfNothingDeleted("playerServerObjects")
            neighbors.deleteOne(Filters.eq(FieldPlayerId, playerId))
                .throwIfNothingDeleted("neighbors")
            inventories.deleteOne(Filters.eq(FieldPlayerId, playerId))
                .throwIfNothingDeleted("inventories")
        }
    }

    override suspend fun shutdown() = Unit
}
