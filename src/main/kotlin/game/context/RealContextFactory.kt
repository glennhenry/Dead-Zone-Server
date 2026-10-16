package game.context

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import encore.account.AccountSubunit
import encore.account.MongoAccountRepository
import encore.acts.ActIdStore
import encore.acts.StageActDirector
import encore.auth.AuthSubunit
import encore.backstage.command.CommandDispatcher
import encore.context.*
import encore.creation.PlayerCreationSubunit
import encore.datastore.MongoDataStore
import encore.extra.player.MongoPlayerExtraRepository
import encore.extra.player.PlayerExtraSubunit
import encore.extra.server.MongoServerExtraRepository
import encore.extra.server.ServerExtraSubunit
import encore.network.lifecycle.PlayerLifecycleHandler
import encore.network.transport.Connection
import encore.presence.PlayerPresenceSubunit
import encore.session.SessionSubunit
import encore.subunit.SubunitLocator
import encore.subunit.scope.PlayerScope
import encore.subunit.scope.ServerScope
import encore.time.TimeCenter
import encore.utils.types.okOrNull
import encore.websocket.WebSocketManager
import game.RealPlayerCreationFactory
import game.domain.compound.MongoCompoundRepository
import game.domain.compound.CompoundSubunit
import game.domain.items.MongoInventoryRepository
import game.domain.items.InventorySubunit
import game.domain.metadata.MongoPlayerObjectsMetadataRepository
import game.domain.metadata.PlayerObjectsMetadataSubunit
import game.domain.survivor.MongoSurvivorRepository
import game.domain.survivor.SurvivorSubunit
import game.mongo.MongoCollections
import game.mongo.collection.Inventory
import game.mongo.collection.PlayerId
import game.mongo.collection.PlayerObjects
import kotlinx.coroutines.CoroutineScope

/**
 * Real implementation of [ContextFactory].
 *
 * Context creation here is user-owned and must be updated accordingly.
 *
 * @property collections Mongo collection names
 * @property mongoDatabase Mongo database
 */
class RealContextFactory(
    private val collections: MongoCollections,
    private val mongoDatabase: MongoDatabase
) : ContextFactory {
    override suspend fun playerContext(
        playerId: PlayerId,
        connection: Connection,
        serverContext: ServerContext
    ): PlayerContext {
        val dataStore = serverContext.dataStore
        val account = requireNotNull(
            serverContext.subunits.account.getAccountByPlayerId(playerId).okOrNull()
        ) {
            "Account not exist during context creation for $playerId"
        }

        val objectsCollection = mongoDatabase.getCollection<PlayerObjects>(collections.playerObjects)
        val inventoryCollection = mongoDatabase.getCollection<Inventory>(collections.inventory)
        val playerObjects = dataStore.playerObjects(playerId)

        val survivor = SurvivorSubunit(
            survivorLeaderId = playerObjects?.playerSurvivor!!,
            survivorRepository = MongoSurvivorRepository(objectsCollection)
        )
        val inventory = InventorySubunit(inventoryRepository = MongoInventoryRepository(inventoryCollection))
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

    override suspend fun serverContext(
        appScope: CoroutineScope,
        serverSubunitScope: ServerScope,
    ): ServerContext {
        /*... setup ServerContext ...*/

        val dataStore = MongoDataStore(db = mongoDatabase, collections = collections)
            .also { it.awaitInit() }

        val accountRepository = MongoAccountRepository(
            accountCollection = mongoDatabase.getCollection(collections.playerAccount)
        )
        val playerExtraRepository = MongoPlayerExtraRepository(mongoDatabase.getCollection(collections.playerServerObjects))
        val serverExtraRepository = MongoServerExtraRepository(mongoDatabase.getCollection(collections.serverObjects))
        val contextRegistry = ContextRegistry(RealContextFactory(collections, mongoDatabase))
        val creationFactory = RealPlayerCreationFactory()
        val stageActDirector = StageActDirector(
            timeSource = TimeCenter.source,
            actStore = ActIdStore
        )
        val commandDispatcher = CommandDispatcher()
        val playerLifecycleHandler = PlayerLifecycleHandler()
        val webSocketManager = WebSocketManager()

        // setup ServerSubunits
        val accountSubunit = AccountSubunit(accountRepository)
        val playerPresenceSubunit = PlayerPresenceSubunit()
        val sessionSubunit = SessionSubunit(appScope, TimeCenter.source)
        val playerCreationSubunit = PlayerCreationSubunit(dataStore, creationFactory)
        val authSubunit = AuthSubunit(accountSubunit, playerCreationSubunit, sessionSubunit)
        val extraPlayer = PlayerExtraSubunit(playerExtraRepository)
        val extraServer = ServerExtraSubunit(serverExtraRepository)

        val subunits = ServerSubunits(
            account = accountSubunit,
            presence = playerPresenceSubunit,
            auth = authSubunit,
            session = sessionSubunit,
            creation = playerCreationSubunit,
            extraPlayer = extraPlayer,
            extraServer = extraServer,
        )

        // debut all subunits
        subunits.debut(serverSubunitScope)

        val serverContext = ServerContext(
            appScope = appScope,
            dataStore = dataStore,
            contextRegistry = contextRegistry,
            stageActDirector = stageActDirector,
            commandDispatcher = commandDispatcher,
            playerLifecycleHandler = playerLifecycleHandler,
            webSocketManager = webSocketManager,
            subunits = subunits,
            locator = SubunitLocator()
        )

        return serverContext
    }
}
