package game

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.context.ContextFactory
import encore.context.PlayerContext
import encore.context.PlayerSubunits
import encore.datastore.DataStore
import encore.datastore.MongoCollectionName
import encore.datastore.collection.PlayerId
import encore.datastore.collection.PlayerObjects
import encore.network.transport.Connection
import encore.subunit.scope.PlayerScope
import game.domain.compound.MongoCompoundRepository
import game.domain.compound.CompoundSubunit
import game.domain.metadata.MongoPlayerObjectsMetadataRepository
import game.domain.metadata.PlayerObjectsMetadataSubunit
import game.domain.survivor.MongoSurvivorRepository
import game.domain.survivor.SurvivorSubunit

/**
 * Real implementation of [ContextFactory].
 *
 * Context creation here is user-owned and must be updated accordingly.
 *
 * @property dataStore [DataStore] instance to retrieve player's data.
 */
class RealContextFactory(
    private val dataStore: DataStore,
    private val collectionName: MongoCollectionName,
    private val mongoDatabase: MongoDatabase
) : ContextFactory {
    override suspend fun createContext(
        playerId: PlayerId,
        connection: Connection
    ): PlayerContext {
        val account = requireNotNull(dataStore.getPlayerAccount(playerId)) {
            "Account not exist on context creation for $playerId"
        }

        val objectsCollection = mongoDatabase.getCollection<PlayerObjects>(collectionName.playerObjects)
        val playerObjects = dataStore.getPlayerObjects(playerId)

        val survivor = SurvivorSubunit(
            survivorLeaderId = playerObjects?.playerSurvivor!!,
            survivorRepository = MongoSurvivorRepository(objectsCollection)
        )
        val inventory =
            game.domain.items.InventorySubunit(inventoryRepository = game.domain.items.InventoryRepositoryMongo())
        val compound = CompoundSubunit(compoundRepository = MongoCompoundRepository(objectsCollection))
        val playerObjectMetadata = PlayerObjectsMetadataSubunit(
            playerObjectsMetadataRepository = MongoPlayerObjectsMetadataRepository(objectsCollection)
        )

        val subunits = PlayerSubunits(
            survivor = survivor,
            compound = compound,
            inventory = inventory,
            playerObjectMetadata = playerObjectMetadata
        )
        subunits.all().forEach { it.debut(PlayerScope(playerId)) }

        return PlayerContext(
            playerId = playerId,
            connection = connection,
            account = account,
            subunits = subunits
        )
    }
}
