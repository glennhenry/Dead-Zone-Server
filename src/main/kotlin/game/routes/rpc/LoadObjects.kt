package game.routes.rpc

import encore.context.ServerContext
import encore.datastore.collection.NeighborHistory
import encore.fancam.Fancam
import encore.utils.types.okOrNull
import game.domain.login.LazyDataUpdater
import game.routes.message.db.BigDBObject
import game.routes.message.db.LoadObjectsArgs
import game.routes.message.db.LoadObjectsOutput
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * LoadObjects (API 85)
 *
 * Input: `[LoadObjectsArgs]`
 *
 * Output: `[LoadObjectsOutput]`
 */
@OptIn(ExperimentalSerializationApi::class)
suspend fun RoutingContext.loadObjects(serverContext: ServerContext) {
    val loadObjectsArgs = ProtoBuf.decodeFromByteArray<LoadObjectsArgs>(
        call.receiveChannel().toByteArray()
    )

    val objs = mutableListOf<BigDBObject>()

    for (objId in loadObjectsArgs.objectIds) {
        val playerId = objId.keys.firstOrNull() ?: continue
        // the game for unknown reason keep requesting the same playerId infinitely
        // this is reflected on id suffixed like id123-2
        val profile = serverContext.subunits.account.getProfile(playerId).okOrNull() ?: continue

        val playerObjects = serverContext.dataStore.getPlayerObjects(playerId)!!
        val neighborHistory = serverContext.dataStore.getNeighbourHistory(playerId)!!
        val inventory = serverContext.dataStore.getInventory(playerId)!!

        val obj: BigDBObject? = when (objId.table) {
            "PlayerObjects" -> {
                // update time-dynamic data
                val updatedBuildings = LazyDataUpdater.updateBuildingTimers(playerObjects.buildings)
                val depletedResources = LazyDataUpdater.depleteResources(profile.lastLogin, playerObjects.resources)
                try {
                    serverContext.dataStore.updatePlayerObjectsField(playerId, "buildings", updatedBuildings)
                    serverContext.dataStore.updatePlayerObjectsField(playerId, "resources", depletedResources)
                } catch (e: Exception) {
                    Fancam.error(e, "loadobjects") { "Error while updating time-dynamic data" }
                    return
                }

                LoadObjectsOutput.fromData(
                    playerObjects.copy(
                        buildings = updatedBuildings,
                        resources = depletedResources
                    )
                )
            }

            "NeighborHistory" -> LoadObjectsOutput.fromData(
                NeighborHistory(
                    playerId = playerId,
                    map = neighborHistory.map
                )
            )

            "Inventory" -> LoadObjectsOutput.fromData(inventory)
            else -> {
                Fancam.error(tag = "loadobjects") { "UNIMPLEMENTED table for ${objId.table}" }
                null
            }
        }

        if (obj != null) objs.add(obj)
    }

    val loadObjectsOutput = ProtoBuf.encodeToByteArray(LoadObjectsOutput(objects = objs))

    call.respondBytes(loadObjectsOutput.pioFraming())
}
