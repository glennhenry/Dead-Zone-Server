package game.socket.handler.save.compound.building

import encore.acts.ActIdStore
import encore.acts.ActScope
import encore.context.ServerContext
import encore.context.requirePlayerContext
import encore.fancam.Fancam
import encore.network.transport.Connection
import encore.serialization.JSON
import encore.utils.identifier.Ids
import encore.utils.types.isFail
import encore.utils.types.isOk
import encore.utils.types.okOrNull
import game.domain.compound.BuildingAct
import game.domain.compound.BuildingActConcept
import game.domain.compound.model.*
import game.domain.model.game.data.TimerData
import game.domain.model.game.data.reduceBy
import game.domain.model.game.data.reduceByHalf
import game.domain.model.game.data.secondsLeftToEnd
import game.socket.handler.buildMsg
import game.socket.handler.save.SaveSubHandler
import game.socket.handler.save.compound.building.response.*
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.SaveDataMethod
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Note about building operation:
 *
 * When building is created/upgraded/repaired:
 * - Client send the request
 * - Server reads that, create a building internally, with the upgrade/repair timer
 * - Timer data depends on operation, it usually includes type (e.g., "repair"/"upgrade"), level, xp
 * - Server should run a background task which fire when the building operation has finished
 * - The completion of that task should send a BUILDING_COMPLETE or BUILDING_REPAIR_COMPLETE message to client
 *
 * When user logoff whilst building operation is ongoing:
 * - Server do not need to save anything, assuming that server already saves everything on operation start.
 *
 * When user login whilst building operation is still on going:
 * - Server does not need to update the upgrade/repair timer, this is because clients read it.
 * - Instead, when the timer has ended, schedule a background task that sends
 * BUILDING_COMPLETE after game is started (which is after INIT_COMPLETE)
 * - The game automatically read the timer and start their countdown of the task.
 *
 * When user login after building operation is done:
 * - Same, no need to update timer. Don't delete it because client reads it.
 * - No need to send BUILDING_COMPLETE, (NOTSURE): but you will likely need to reflect the building operation, maybe during PlayerLoginState?
 */
class BuildingSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.COMPOUND_BUILDING_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        val playerId = connection.playerId

        when (type) {
            // read buildings.xml from GameDefinition for items/build duration
            SaveDataMethod.BUILDING_CREATE -> {
                val bldId = data["id"] as? String ?: return
                val bldType = data["type"] as? String? ?: return
                val x = data["tx"] as? Int ?: return
                val y = data["ty"] as? Int ?: return
                val r = data["rotation"] as? Int ?: return

                Fancam.debug(tag = "save_bld") { "'BUILDING_CREATE' message for $saveId and $bldId,$bldType to tx=$x, ty=$y, rotation=$r" }

                val buildDuration = if (bldType.contains("storage")) 1.minutes else 4.seconds

                val timer = TimerData.runForDuration(
                    duration = buildDuration,
                    // xp used when reconnect, readObject of building
                    data = mapOf("level" to 0, "type" to "upgrade", "xp" to 50)
                )

                val svc = serverContext.requirePlayerContext(playerId).subunits
                val report = svc.compound.createBuilding {
                    Building(
                        id = bldId,
                        name = null,
                        type = bldType,
                        level = 0, // always 0 because create
                        rotation = r,
                        tx = x,
                        ty = y,
                        destroyed = false,
                        resourceValue = 0.0,
                        upgrade = timer, // create can be thought as an upgrade to level 0
                        repair = null
                    )
                }

                val response: BuildingCreateResponse
                if (report.isOk()) {
                    response = BuildingCreateResponse(
                        success = true,
                        items = emptyMap(),
                        timer = timer
                    )
                } else {
                    Fancam.error(tag = "save_bld") { "Failed to create building type=$type bldId=$bldId for playerId=$playerId" }
                    response = BuildingCreateResponse(
                        success = false,
                        items = emptyMap(),
                        timer = null
                    )
                }

                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))

                if (report.isOk()) {
                    val actId = serverContext.stageActDirector.run(
                        act = BuildingAct(),
                        concept = BuildingActConcept(
                            connection,
                            NetworkMessage.BUILDING_COMPLETE,
                            bldId,
                            buildDuration
                        ),
                        scope = ActScope(playerId, connection.connectionScope)
                    )
                    ActIdStore.bind(actId, Ids.join(playerId, bldId))
                }
            }

            SaveDataMethod.BUILDING_MOVE -> {
                val x = (data["tx"] as? Number)?.toInt() ?: return
                val y = (data["ty"] as? Number)?.toInt() ?: return
                val r = (data["rotation"] as? Number)?.toInt() ?: return
                val buildingId = data["id"] as? String ?: return

                Fancam.debug(tag = "save_bld") { "'bld_move' message for $saveId and $buildingId to tx=$x, ty=$y, rotation=$r" }

                val svc = serverContext.requirePlayerContext(playerId).subunits
                val report = svc.compound.updateBuilding(buildingId) {
                    it.copy(tx = x, ty = y, rotation = r)
                }

                val response: BuildingMoveResponse
                if (report.isOk()) {
                    response = BuildingMoveResponse(success = true, x = x, y = y, r = r)
                } else {
                    Fancam.error(tag = "save_bld") { "Failed to move building bldId=$buildingId for playerId=$playerId" }
                    response = BuildingMoveResponse(success = false, x = x, y = y, r = r)
                }

                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.BUILDING_UPGRADE -> {
                val bldId = data["id"] as? String ?: return
                Fancam.debug(tag = "save_bld") { "'BUILDING_UPGRADE' message for $saveId and $bldId" }

                val buildDuration = 10.seconds
                lateinit var timer: TimerData
                val svc = serverContext.requirePlayerContext(playerId).subunits

                val report = svc.compound.updateBuilding(bldId) { bld ->
                    timer = TimerData.runForDuration(
                        duration = buildDuration,
                        data = mapOf("level" to (bld.level + 1), "type" to "upgrade", "xp" to 50)
                    )
                    bld.copy(upgrade = timer)
                }

                val response: BuildingUpgradeResponse
                if (report.isOk()) {
                    response = BuildingUpgradeResponse(success = true, items = emptyMap(), timer = timer)
                } else {
                    Fancam.error(tag = "save_bld") { "Failed to upgrade building bldId=$bldId for playerId=$playerId" }
                    response = BuildingUpgradeResponse(success = false, items = emptyMap(), timer = null)
                }

                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))

                if (report.isOk()) {
                    serverContext.stageActDirector.run(
                        act = BuildingAct(),
                        concept = BuildingActConcept(
                            connection,
                            NetworkMessage.BUILDING_COMPLETE,
                            bldId,
                            buildDuration
                        ),
                        scope = ActScope(playerId, connection.connectionScope)
                    )
                }
            }

            SaveDataMethod.BUILDING_RECYCLE -> {
                val bldId = data["id"] as? String ?: return
                Fancam.debug(tag = "save_bld") { "'BUILDING_RECYCLE' message for $saveId and $bldId" }

                val svc = serverContext.requirePlayerContext(playerId).subunits
                val report = svc.compound.deleteBuilding(bldId)

                val response: BuildingRecycleResponse
                if (report.isOk()) {
                    response = BuildingRecycleResponse(success = true, items = emptyMap())
                } else {
                    Fancam.error(tag = "save_bld") { "Failed to recycle building bldId=$bldId for playerId=$playerId" }
                    response = BuildingRecycleResponse(success = false, items = emptyMap())
                }

                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.BUILDING_COLLECT -> {
                val bldId = data["id"] as? String ?: return
                Fancam.debug(tag = "save_bld") { "'BUILDING_COLLECT' message for $saveId and $bldId" }

                val svc = serverContext.requirePlayerContext(playerId).subunits
                val resources = svc.compound.collectBuilding(bldId).okOrNull()

                val response: BuildingCollectResponse
                if (resources != null) {
                    val resType =
                        requireNotNull(resources.getNonEmptyResTypeOrNull()) {
                            "Unexpected null on getNonEmptyResTypeOrNull during collect resource"
                        }
                    val resAmount =
                        requireNotNull(resources.getNonEmptyResAmountOrNull()?.toDouble()) {
                            "Unexpected null on getNonEmptyResAmountOrNull during collect resource"
                        }

                    val currentResource = svc.compound.getResources()
                    val limit = 100_000_000.0 // this based on storage
                    val expectedResource = currentResource.wood + resAmount
                    val remainder = expectedResource - limit
                    val total = min(limit, expectedResource)

                    response = BuildingCollectResponse(
                        success = true,
                        locked = false,
                        resource = resType,
                        collected = resAmount,
                        remainder = remainder, // - with current resource
                        total = total, // + with current resource
                        bonus = 0.0,
                        destroyed = false,
                    )
                } else {
                    Fancam.error(tag = "save_bld") { "Failed to collect building bldId=$bldId for playerId=$playerId" }
                    response = BuildingCollectResponse(
                        success = false,
                        locked = false,
                        resource = "",
                        collected = 0.0,
                        remainder = 0.0,
                        total = 0.0,
                        bonus = 0.0,
                        destroyed = false
                    )
                }

                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.BUILDING_CANCEL -> {
                val bldId = data["id"] as? String ?: return
                Fancam.debug(tag = "save_bld") { "'BUILDING_CANCEL' message for $saveId and $bldId" }

                val svc = serverContext.requirePlayerContext(playerId).subunits
                val report = svc.compound.cancelBuilding(bldId)

                val response: BuildingCancelResponse
                if (report.isOk()) {
                    response = BuildingCancelResponse(success = true, items = emptyMap())
                } else {
                    Fancam.error(tag = "save_bld") { "Failed to cancel building bldId=$bldId for playerId=$playerId" }
                    response = BuildingCancelResponse(success = false, items = emptyMap())
                }

                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.BUILDING_SPEED_UP -> {
                val bldId = data["id"] as? String ?: return
                val option = data["option"] as? String ?: return
                Fancam.debug(tag = "save_bld") { "'BUILDING_SPEED_UP' message for bldId=$bldId with option.key=$option" }

                val svc = serverContext.requirePlayerContext(connection.playerId).subunits
                val playerFuel = svc.compound.getResources().cash
                val notEnoughCoinsErrorId = "55"

                val response: BuildingSpeedUpResponse
                var resourceResponse: GameResources? = null
                if (playerFuel < 0) {
                    response = BuildingSpeedUpResponse(error = notEnoughCoinsErrorId, success = false, cost = 0)
                } else {
                    val building =
                        requireNotNull(svc.compound.getBuildingById(bldId)) { "Building bldId=$bldId was somehow null in BUILDING_SPEED_UP request for playerId=$playerId" }.toBuilding()
                    val upgradeTimer =
                        requireNotNull(building.upgrade) { "Building upgrade timer for bldId=$bldId was somehow null in BUILDING_SPEED_UP request for playerId=$playerId" }

                    // TO-DO ensure that the selected option is enabled in the cost table.
                    // this prevent player from manipulating packet to unknown speed up option
                    // TO-DO: calculate the cost in cost table
                    val (newBuilding, cost) = when (option) {
                        "SpeedUpOneHour" -> {
                            building.copy(upgrade = upgradeTimer.reduceBy(1.hours)) to 1
                        }

                        "SpeedUpTwoHour" -> {
                            building.copy(upgrade = upgradeTimer.reduceBy(2.hours)) to 1
                        }

                        "SpeedUpHalf" -> {
                            building.copy(upgrade = upgradeTimer.reduceByHalf()) to 1
                        }

                        "SpeedUpComplete" -> {
                            building.copy(upgrade = null) to 1
                        }

                        "SpeedUpFree" -> {
                            // TO-DO lookup cost table and see the minimum time to speed up for free
                            // in this case, only if building duration is less than 5 minutes it will be allowed
                            if (building.upgrade.secondsLeftToEnd() <= 300) {
                                building.copy(upgrade = null) to 1
                            } else {
                                Fancam.warn {
                                    "Received unexpected BuildingSpeedUp FREE option: $option from playerId=${connection.playerId} (speed up requested when timer is off or build time more than 5 minutes)"
                                }
                                null to null
                            }
                        }

                        else -> {
                            Fancam.warn {
                                "Received unknown BuildingSpeedUp option: $option from playerId=${connection.playerId}"
                            }
                            null to null
                        }
                    }

                    if (newBuilding != null && cost != null) {
                        // successful response
                        val updateBuildingResult = svc.compound.updateBuilding(bldId) { newBuilding as BuildingLike }
                        val updateResourceResult = svc.compound.updateResources { resource ->
                            resourceResponse = resource.copy(cash = playerFuel - cost)
                            resourceResponse
                        }

                        // if for some reason DB fail, do not proceed the speed up request
                        response = if (updateBuildingResult.isFail() || updateResourceResult.isFail()) {
                            BuildingSpeedUpResponse(error = "", success = false, cost = 0)
                        } else {
                            BuildingSpeedUpResponse(error = "", success = true, cost = cost)
                        }

                        // end the currently active building task
                        val actId =
                            requireNotNull(ActIdStore.find(bldId)) { "Null during ActIdStore.find(bldId=$bldId)" }
                        serverContext.stageActDirector.stop(actId)

                        // then restart it to change the timer
                        // if construction ended after the speed up, automatically start with zero second delay
                        serverContext.stageActDirector.run(
                            act = BuildingAct(),
                            concept = BuildingActConcept(
                                connection,
                                NetworkMessage.BUILDING_COMPLETE,
                                bldId,
                                newBuilding.upgrade
                                    ?.secondsLeftToEnd()
                                    ?.toDuration(DurationUnit.SECONDS)
                                    ?: Duration.ZERO
                            ),
                            scope = ActScope(playerId, connection.connectionScope)
                        )
                    } else {
                        // unexpected DB error response
                        Fancam.error(tag = "save_bld") { "Failed to speed up create building bldId=$bldId for playerId=$playerId: old=${building.toCompactString()} new=${newBuilding?.toCompactString()}" }
                        response = BuildingSpeedUpResponse(error = "", success = false, cost = 0)
                    }
                }

                val responseJson = JSON.encode(response)
                val resourceResponseJson = JSON.encode(resourceResponse)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson, resourceResponseJson)))
            }

            SaveDataMethod.BUILDING_REPAIR -> {
                val bldId = data["id"] as? String ?: return
                Fancam.debug(tag = "save_bld") { "'BUILDING_REPAIR' message for $saveId and $bldId" }

                val buildDuration = 10.seconds
                val timer = TimerData.runForDuration(
                    duration = buildDuration,
                    data = mapOf("type" to "repair")
                )

                val svc = serverContext.requirePlayerContext(playerId).subunits
                val report = svc.compound.updateBuilding(bldId) { bld ->
                    bld.copy(repair = timer)
                }

                val response: BuildingRepairResponse
                if (report.isOk()) {
                    response = BuildingRepairResponse(success = true, items = emptyMap(), timer = timer)
                } else {
                    Fancam.error(tag = "save_bld") { "Failed to repair building bldId=$bldId for playerId=$playerId" }
                    response = BuildingRepairResponse(success = false, items = emptyMap(), timer = null)
                }

                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))

                if (report.isOk()) {
                    serverContext.stageActDirector.run(
                        act = BuildingAct(),
                        concept = BuildingActConcept(
                            connection,
                            NetworkMessage.BUILDING_REPAIR_COMPLETE,
                            bldId,
                            buildDuration
                        ),
                        scope = ActScope(playerId, connection.connectionScope)
                    )
                }
            }

            SaveDataMethod.BUILDING_REPAIR_SPEED_UP -> {
                val bldId = data["id"] as? String ?: return
                val option = data["option"] as? String ?: return
                Fancam.debug(tag = "save_bld") { "'BUILDING_REPAIR_SPEED_UP' message for $saveId and $bldId with option.key=$option" }

                val svc = serverContext.requirePlayerContext(connection.playerId).subunits
                val playerFuel = svc.compound.getResources().cash
                val notEnoughCoinsErrorId = "55"

                var resourceResponse: GameResources? = null
                val response: BuildingRepairSpeedUpResponse
                if (playerFuel < 0) {
                    response = BuildingRepairSpeedUpResponse(error = notEnoughCoinsErrorId, success = false, cost = 0)
                } else {
                    val building =
                        requireNotNull(svc.compound.getBuildingById(bldId)) { "Building bldId=$bldId was somehow null in BUILDING_REPAIR_SPEED_UP request for playerId=$playerId" }.toBuilding()
                    val repairTimer =
                        requireNotNull(building.repair) { "Building repair timer for bldId=$bldId was somehow null in BUILDING_REPAIR_SPEED_UP request for playerId=$playerId" }

                    // TO-DO ensure that the selected option is enabled in the cost table.
                    // this prevent player from manipulating packet to unknown speed up option
                    // TO-DO: calculate the cost in cost table
                    val (newBuilding, cost) = when (option) {
                        "SpeedUpOneHour" -> {
                            building.copy(repair = repairTimer.reduceBy(1.hours)) to 1
                        }

                        "SpeedUpTwoHour" -> {
                            building.copy(repair = repairTimer.reduceBy(2.hours)) to 1
                        }

                        "SpeedUpHalf" -> {
                            building.copy(repair = repairTimer.reduceByHalf()) to 1
                        }

                        "SpeedUpComplete" -> {
                            building.copy(repair = null) to 1
                        }

                        "SpeedUpFree" -> {
                            // TO-DO lookup cost table and see the minimum time to speed up for free
                            // in this case, only if building duration is less than 5 minutes it will be allowed
                            if (building.repair.secondsLeftToEnd() <= 300) {
                                building.copy(repair = null) to 1
                            } else {
                                Fancam.warn { "Received unexpected BuildingSpeedUp FREE option: $option from playerId=${connection.playerId} (speed up requested when timer is off or build time more than 5 minutes)" }
                                null to null
                            }
                        }

                        else -> {
                            Fancam.warn { "Received unknown BuildingRepairSpeedUp option: $option from playerId=${connection.playerId}" }
                            null to null
                        }
                    }

                    if (newBuilding != null && cost != null) {
                        // successful response
                        val updateBuildingResult = svc.compound.updateBuilding(bldId) { newBuilding as BuildingLike }
                        val updateResourceResult = svc.compound.updateResources { resource ->
                            resourceResponse = resource.copy(cash = playerFuel - cost)
                            resourceResponse
                        }

                        // if for some reason DB fail, do not proceed the speed up request
                        response = if (updateBuildingResult.isFail() || updateResourceResult.isFail()) {
                            BuildingRepairSpeedUpResponse(error = "", success = false, cost = 0)
                        } else {
                            BuildingRepairSpeedUpResponse(error = "", success = true, cost = cost)
                        }

                        // end the currently active building repair task
                        val actId =
                            requireNotNull(ActIdStore.find(bldId)) { "Null during ActIdStore.find(bldId=$bldId)" }
                        serverContext.stageActDirector.stop(actId)

                        // then restart it to change the timer
                        // if construction ended after the speed up, automatically start with zero second delay
                        serverContext.stageActDirector.run(
                            act = BuildingAct(),
                            concept = BuildingActConcept(
                                connection,
                                NetworkMessage.BUILDING_COMPLETE,
                                bldId,
                                newBuilding.repair
                                    ?.secondsLeftToEnd()
                                    ?.toDuration(DurationUnit.SECONDS)
                                    ?: Duration.ZERO
                            ),
                            scope = ActScope(playerId, connection.connectionScope)
                        )
                    } else {
                        // unexpected DB error response
                        Fancam.error(tag = "save_bld") { "Failed to speed up repair building bldId=$bldId for playerId=$playerId: old=${building.toCompactString()} new=${newBuilding?.toCompactString()}" }
                        response = BuildingRepairSpeedUpResponse(error = "", success = false, cost = 0)
                    }
                }

                val responseJson = JSON.encode(response)
                val resourceResponseJson = JSON.encode(resourceResponse)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson, resourceResponseJson)))
            }

            SaveDataMethod.BUILDING_CREATE_BUY -> {
                val bldId = data["id"] as? String ?: return
                val bldType = data["type"] as? String ?: return
                val x = data["tx"] as? Int ?: return
                val y = data["ty"] as? Int ?: return
                val r = data["rotation"] as? Int ?: return
                Fancam.debug(tag = "save_bld") { "'BUILDING_CREATE_BUY' message for $saveId and $bldId,$bldType to tx=$x, ty=$y, rotation=$r" }

                val buildDuration = 0.seconds
                val timer = TimerData.runForDuration(
                    duration = buildDuration,
                    // xp used when reconnect, readObject of building
                    data = mapOf("level" to 0, "type" to "upgrade", "xp" to 50)
                )

                val svc = serverContext.requirePlayerContext(playerId).subunits
                val report = svc.compound.createBuilding {
                    Building(
                        id = bldId,
                        name = null,
                        type = bldType,
                        level = 0, // always 0 because create
                        rotation = r,
                        tx = x,
                        ty = y,
                        destroyed = false,
                        resourceValue = 0.0,
                        upgrade = timer, // create can be thought as an upgrade to level 0
                        repair = null
                    )
                }

                val response: BuildingCreateResponse
                if (report.isOk()) {
                    response = BuildingCreateResponse(
                        success = true,
                        items = emptyMap(),
                        timer = timer
                    )
                } else {
                    Fancam.error(tag = "save_bld") { "Failed to create (buy) building bldId=$bldId for playerId=$playerId" }
                    response = BuildingCreateResponse(
                        success = false,
                        items = emptyMap(),
                        timer = null
                    )
                }

                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))

                if (report.isOk()) {
                    serverContext.stageActDirector.run(
                        act = BuildingAct(),
                        concept = BuildingActConcept(
                            connection,
                            NetworkMessage.BUILDING_COMPLETE,
                            bldId,
                            buildDuration
                        ),
                        scope = ActScope(playerId, connection.connectionScope)
                    )
                }
            }

            SaveDataMethod.BUILDING_UPGRADE_BUY -> {
                val bldId = data["id"] as? String ?: return
                Fancam.debug(tag = "save_bld") { "'BUILDING_UPGRADE_BUY' message for $saveId and $bldId" }

                val buildDuration = 0.seconds

                lateinit var timer: TimerData
                val svc = serverContext.requirePlayerContext(playerId).subunits
                val report = svc.compound.updateBuilding(bldId) { bld ->
                    timer = TimerData.runForDuration(
                        duration = buildDuration,
                        data = mapOf("level" to (bld.level + 1), "type" to "upgrade", "xp" to 50)
                    )
                    bld.copy(upgrade = timer)
                }

                val response: BuildingUpgradeResponse
                if (report.isOk()) {
                    response = BuildingUpgradeResponse(success = true, items = emptyMap(), timer = timer)
                } else {
                    Fancam.error(tag = "save_bld") { "Failed to upgrade (buy) building bldId=$bldId for playerId=$playerId" }
                    response = BuildingUpgradeResponse(success = false, items = emptyMap(), timer = null)
                }

                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))

                if (report.isOk()) {
                    serverContext.stageActDirector.run(
                        act = BuildingAct(),
                        concept = BuildingActConcept(
                            connection,
                            NetworkMessage.BUILDING_COMPLETE,
                            bldId,
                            buildDuration
                        ),
                        scope = ActScope(playerId, connection.connectionScope)
                    )
                }
            }

            SaveDataMethod.BUILDING_REPAIR_BUY -> {
                Fancam.warn(tag = "save") { "Received 'BUILDING_REPAIR_BUY' message [not implemented]" }
            }

            SaveDataMethod.BUILDING_TRAP_EXPLODE -> {
                Fancam.warn(tag = "save") { "Received 'BUILDING_TRAP_EXPLODE' message [not implemented]" }
            }
        }
    }
}
