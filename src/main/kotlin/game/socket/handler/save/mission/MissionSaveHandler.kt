package game.socket.handler.save.mission

import encore.context.ServerContext
import encore.context.requirePlayerContext
import encore.fancam.Fancam
import encore.network.transport.Connection
import encore.serialization.JSON
import game.core.model.game.data.ZombieData
import game.core.model.game.data.toFlatList
import game.domain.mission.LootGenerator
import game.domain.mission.model.LootParameter
import game.socket.handler.buildMsg
import game.socket.handler.save.SaveSubHandler
import game.socket.handler.save.mission.response.*
import game.socket.protocol.PIOSerializer
import game.socket.protocol.SaveDataMethod
import kotlin.random.Random

class MissionSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.MISSION_SAVES

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
            SaveDataMethod.MISSION_START -> {
                // IMPORTANT NOTE: the scene that involves human model is not working now (e.g., raid island human)
                // the same error is for survivor class if you fill SurvivorAppearance non-null value
                // The error was 'cyclic object' thing.
                val isCompoundZombieAttack = data["compound"]?.equals(true)
                val areaType = if (isCompoundZombieAttack == true) "compound" else data["areaType"] as String
                Fancam.info(tag = "save") { "Going to scene with areaType=$areaType" }

                val svc = serverContext.requirePlayerContext(playerId).subunits
                val leader = svc.survivor.getSurvivorLeader()

                val sceneXML = resolveAndLoadScene(areaType)
                if (sceneXML == null) {
                    Fancam.error(tag = "save") { "That area=$areaType isn't working yet, typically because the map file is lost" }
                    return
                }
                val lootParameter = LootParameter(
                    areaLevel = (data["areaLevel"] as? Int ?: 0),
                    playerLevel = leader.level,
                    itemWeightOverrides = mapOf(),
                    specificItemBoost = mapOf(
                        "fuel-bottle" to 3.0,    // +300% find fuel chance (of the base chance)
                        "fuel-container" to 3.0,
                        "fuel" to 3.0,
                        "fuel-cans" to 3.0,
                    ),
                    itemTypeBoost = mapOf(
                        "junk" to 0.8 // +80% junk find chance
                    ),
                    itemQualityBoost = mapOf(
                        "blue" to 0.5 // +50% blue quality find chance
                    ),
                    baseWeight = 1.0,
                    fuelLimit = 50
                )
                val lootService = LootGenerator(sceneXML, lootParameter)
                val sceneXMLWithLoot = lootService.insertLoots()

                val zombies = listOf(
                    ZombieData.standardZombieWeakAttack(Random.nextInt()),
                    ZombieData.standardZombieWeakAttack(Random.nextInt()),
                    ZombieData.standardZombieWeakAttack(Random.nextInt()),
                    ZombieData.standardZombieWeakAttack(Random.nextInt()),
                    ZombieData.dogStandard(Random.nextInt()),
                    ZombieData.dogStandard(Random.nextInt()),
                    ZombieData.fatWalkerStrongAttack(Random.nextInt()),
                ).flatMap { it.toFlatList() }

                val timeSeconds = if (isCompoundZombieAttack == true) 30 else 240

                val responseJson = JSON.encode(
                    MissionStartResponse(
                        id = saveId,
                        time = timeSeconds,
                        assignmentType = "None", // 'None' because not a raid or arena. see AssignmentType
                        areaClass = (data["areaClass"] as String?) ?: "", // supposedly depend on the area
                        automated = false, // this depends on request data
                        sceneXML = sceneXMLWithLoot,
                        z = zombies,
                        allianceAttackerEnlisting = false,
                        allianceAttackerLockout = false,
                        allianceAttackerAllianceId = null,
                        allianceAttackerAllianceTag = null,
                        allianceMatch = false,
                        allianceRound = 0,
                        allianceRoundActive = false,
                        allianceError = false,
                        allianceAttackerWinPoints = 0
                    )
                )

                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            // mis_startFlag and mis_interacted do not expect a response
            SaveDataMethod.MISSION_START_FLAG -> {
                Fancam.info { "<----- Mission start flag received ----->" }
            }

            SaveDataMethod.MISSION_INTERACTION_FLAG -> {
                Fancam.info { "<----- First interaction received ----->" }
            }

            SaveDataMethod.MISSION_END -> {
                val svc = serverContext.requirePlayerContext(playerId).subunits
                val leader = svc.survivor.getSurvivorLeader()

                // some of most important data
                val responseJson = JSON.encode(
                    MissionEndResponse(
                        automated = false,
                        xpEarned = 100,
                        xp = null,
                        returnTimer = null,
                        lockTimer = null,
                        loot = emptyList(),
                        // item id to quantity
                        itmCounters = emptyMap(),
                        injuries = null,
                        // the survivors that goes into the mission
                        survivors = emptyList(),
                        player = PlayerSurvivor(xp = 100, level = leader.level),
                        levelPts = 0,
                        // base64 encoded string
                        cooldown = null
                    )
                )

                // change resource with obtained loot...
                val currentResource = svc.compound.getResources()

                val resourceResponseJson = JSON.encode(currentResource)

                send(PIOSerializer.serialize(buildMsg(saveId, responseJson, resourceResponseJson)))
            }

            SaveDataMethod.MISSION_ZOMBIES -> {
                // usually requested during middle of mission
                // there could be 'rush' flag somewhere, which means we need to send runner zombies

                val zombies = listOf(
                    ZombieData.strongRunner(Random.nextInt()),
                    ZombieData.strongRunner(Random.nextInt()),
                    ZombieData.standardZombieWeakAttack(Random.nextInt()),
                    ZombieData.standardZombieWeakAttack(Random.nextInt()),
                    ZombieData.fatWalkerStrongAttack(104),
                    ZombieData.fatWalkerStrongAttack(105),
                ).flatMap { it.toFlatList() }

                val responseJson = JSON.encode(
                    GetZombieResponse(
                        max = false,
                        z = zombies
                    )
                )

                Fancam.info(tag = "save") { "'mis_zombies' message (spawn zombie) request received" }

                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.MISSION_INJURY -> {
                Fancam.warn(tag = "save") { "Received 'MISSION_INJURY' message [not implemented]" }
            }

            SaveDataMethod.MISSION_SPEED_UP -> {
                Fancam.warn(tag = "save") { "Received 'MISSION_SPEED_UP' message [not implemented]" }
            }

            SaveDataMethod.MISSION_SCOUTED -> {
                Fancam.warn(tag = "save") { "Received 'MISSION_SCOUTED' message [not implemented]" }
            }

            SaveDataMethod.MISSION_ITEM_USE -> {
                Fancam.warn(tag = "save") { "Received 'MISSION_ITEM_USE' message [not implemented]" }
            }

            SaveDataMethod.MISSION_TRIGGER -> {
                Fancam.warn(tag = "save") { "Received 'MISSION_TRIGGER' message [not implemented]" }
            }

            SaveDataMethod.MISSION_ELITE_SPAWNED -> {
                Fancam.warn(tag = "save") { "Received 'MISSION_ELITE_SPAWNED' message [not implemented]" }
            }

            SaveDataMethod.MISSION_ELITE_KILLED -> {
                Fancam.warn(tag = "save") { "Received 'MISSION_ELITE_KILLED' message [not implemented]" }
            }

            // also handle this
            SaveDataMethod.STAT_DATA -> {
                val stats = data["stats"]
                Fancam.debug { data["stats"].toString() }
                Fancam.warn(tag = "save") { "Received 'STAT_DATA' message on MissionSaveHandler [not implemented] with stats: $stats" }
            }

            SaveDataMethod.STAT -> {
                val stats = data["stats"]
                Fancam.debug { data["stats"].toString() }
                Fancam.warn(tag = "save") { "Received 'STAT_DATA' message on MissionSaveHandler [not implemented] with stats: $stats" }
            }
        }
    }
}
