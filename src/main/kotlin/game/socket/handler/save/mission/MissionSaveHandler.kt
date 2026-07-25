package game.socket.handler.save.mission

import encore.acts.ActScope
import game.context.ServerContext
import game.context.requirePlayerContext
import encore.definition.GameReference
import encore.fancam.Fancam
import encore.network.transport.Connection
import encore.serialization.JSON
import encore.utils.identifier.Ids
import game.core.model.game.data.ZombieData
import game.core.model.game.data.toFlatList
import game.definition.ItemsDefinition
import game.domain.compound.model.GameResources
import game.domain.items.model.Item
import game.domain.items.model.combineItems
import game.domain.items.model.compactString
import game.domain.items.model.stackOwnItems
import game.domain.mission.LootService
import game.domain.mission.MissionReturnAct
import game.domain.mission.MissionReturnActConcept
import game.domain.mission.model.LootContent
import game.domain.mission.model.LootParameter
import game.domain.mission.model.MissionStats
import game.domain.model.game.data.TimerData
import game.socket.handler.buildMsg
import game.socket.handler.save.SaveSubHandler
import game.socket.handler.save.mission.response.*
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.SaveDataMethod
import game.socket.sendPIOMessage
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

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
                val lootService = LootService(sceneXML, lootParameter)
                val (sceneXMLWithLoot, insertedLoots) = lootService.insertLoots()

                val zombies = listOf(
                    ZombieData.standardZombieWeakAttack(Random.nextInt()),
                    ZombieData.standardZombieWeakAttack(Random.nextInt()),
                    ZombieData.dogStandard(Random.nextInt()),
                    ZombieData.fatWalkerStrongAttack(Random.nextInt()),
                ).flatMap { it.toFlatList() }

                val timeSeconds = if (isCompoundZombieAttack == true) 30 else 240

                // keep track of insertedLoots for player that is on a mission
                // this will be used on MISSION_END
                connection.put("loots", insertedLoots)

                // generate unique identifier as missionId
                val missionId = Ids.uuid()
                connection.put("missionId", missionId)

                val responseJson = JSON.encode(
                    MissionStartResponse(
                        id = missionId,
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

                val playerStats = requireNotNull(connection.get("stats") as? MissionStats) { "mission stats is null on MISSION_END request" }
                val earnedXp = calculateMissionXp(playerStats.killData)

                val missionId =
                    requireNotNull(connection.get("missionId") as? String) {
                        "Mission ID for playerId=$playerId was somehow null in MISSION_END request. (raw=${
                            connection.get(
                                "missionId"
                            )
                        })"
                    }

                @Suppress("UNCHECKED_CAST")
                val insertedLoots =
                    requireNotNull(connection.get("loots") as? List<LootContent>) {
                        "Mission ID for playerId=$playerId was somehow null in MISSION_END request. (raw=${
                            connection.get(
                                "loots"
                            )
                        })"
                    }

                val rawLootedItems = summarizeLoots(data, insertedLoots)
                val (combinedLootedItems, obtainedResources) = buildInventoryAndResource(rawLootedItems)

                // Calculate new XP and level
                val newXp = leader.xp + earnedXp
                val (newLevel, newLevelPts) = calculateNewLevelAndPoints(leader.level, leader.xp, newXp)

                // Update the leader's XP and level
                // TODO respond to DB failure
                svc.survivor.updateSurvivor(leader.id) { currentLeader ->
                    currentLeader.copy(xp = newXp, level = newLevel)
                }


                // Update player's inventory
                // TO-DO move inventory update to MissionReturnTask execute()
                // items and injuries are sent to player after mission return complete
                // TODO respond to DB failure
                svc.inventory.updateInventory { items ->
                    items.combineItems(
                        combinedLootedItems.filter { !GameReference.get<ItemsDefinition>().isResourceItem(it.type) },
                        GameReference
                    )
                }

                // TODO respond to DB failure
                svc.compound.updateResources { currentRes ->
                    // Cap resources at storage limit (default 100 per resource type)
                    val storageLimit =
                        100_000_000 // TODO: Get actual limit from GameDefinitions based on storage buildings
                    val cappedResources = GameResources(
                        wood = minOf(currentRes.wood + obtainedResources.wood, storageLimit),
                        metal = minOf(currentRes.metal + obtainedResources.metal, storageLimit),
                        cloth = minOf(currentRes.cloth + obtainedResources.cloth, storageLimit),
                        water = minOf(currentRes.water + obtainedResources.water, storageLimit),
                        food = minOf(currentRes.food + obtainedResources.food, storageLimit),
                        ammunition = minOf(currentRes.ammunition + obtainedResources.ammunition, storageLimit),
                        cash = currentRes.cash + obtainedResources.cash // Cash has no limit
                    )
                    cappedResources
                }

                val returnTime = 20.seconds

                val responseJson = JSON.encode(
                    MissionEndResponse(
                        automated = false,
                        xpEarned = earnedXp,
                        xp = XpBreakdown(total = earnedXp),
                        returnTimer = TimerData.runForDuration(
                            duration = returnTime,
                            data = mapOf("return" to returnTime.toInt(DurationUnit.SECONDS))
                        ),
                        lockTimer = null,
                        loot = combinedLootedItems,

                        // itmCounters is not related to item quantity
                        // it is some kind of internal weapon state (i.e., kill count)
                        itmCounters = emptyMap(),
                        injuries = null,
                        survivors = emptyList(),
                        player = PlayerSurvivor(
                            xp = newXp,
                            level = newLevel
                        ),
                        levelPts = newLevelPts,
                        cooldown = null
                    )
                )

                val resourceResponseJson = JSON.encode(svc.compound.getResources())
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson, resourceResponseJson)))

                // TO-DO update player's task collection to include the mission return task
                serverContext.stageActDirector.run(
                    act = MissionReturnAct(),
                    concept = MissionReturnActConcept(
                        connection = connection,
                        missionId = missionId,
                        returnTime = 20.seconds // TO-DO should be based on area level
                    ),
                    scope = ActScope(playerId, connection.connectionScope)
                )

                // remove previously put temp data
                connection.delete("stats")
                connection.delete("loots")
                connection.delete("missionId")
            }

            SaveDataMethod.MISSION_ZOMBIES -> {
                // usually requested during middle of mission
                // there could be 'rush' flag somewhere, which means we need to send runner zombies

                val zombies = listOf(
                    ZombieData.strongRunner(Random.nextInt()),
                    ZombieData.standardZombieWeakAttack(Random.nextInt()),
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
                Fancam.info(tag = "mis_save") { "Received 'MISSION_SPEED_UP' message" }

                // TO-DO implement mission speed up similar to building speed up
                // TO-DO don't forget to save mission as a task to DB (TaskCollection),
                // just like how building construction modify upgrade/repair timer in DB

                // temporarily always make speed up always success so player don't stuck
                val response = MissionSpeedUpResponse("", true, 0)
                val responseJson = JSON.encode(response)
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))

                val missionId = requireNotNull(connection.get("missionId")) { "missionId is null on MISSION_SPEED_UP" }
                connection.sendPIOMessage(NetworkMessage.MISSION_RETURN_COMPLETE, missionId)
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
                val playerStats = parseMissionStats(data["stats"])
                Fancam.debug { "STAT_DATA parsed: $playerStats" }
                connection.put("stats", playerStats)
            }

            SaveDataMethod.STAT -> {
                val playerStats = parseMissionStats(data["stats"])
                Fancam.debug { "STAT parsed: ${connection.get("stats")}" }
                connection.put("stats", playerStats)
            }
        }
    }

    private fun parseMissionStats(raw: Any?): MissionStats {
        val m = (raw as? Map<*, *>) ?: emptyMap<Any?, Any?>()
        fun asInt(v: Any?): Int = when (v) {
            is Int -> v
            is Long -> v.toInt()
            is Double -> v.toInt()
            is Float -> v.toInt()
            is Number -> v.toInt()
            is String -> v.toIntOrNull() ?: 0
            else -> 0
        }

        fun asDouble(v: Any?): Double = when (v) {
            is Double -> v
            is Float -> v.toDouble()
            is Int -> v.toDouble()
            is Long -> v.toDouble()
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

        val knownKeys = setOf(
            "zombieSpawned", "levelUps", "damageOutput", "damageTaken", "containersSearched",
            "survivorKills", "survivorsDowned", "survivorExplosiveKills",
            "humanKills", "humanExplosiveKills",
            "zombieKills", "zombieExplosiveKills",
            "hpHealed", "explosivesPlaced", "grenadesThrown", "grenadesSmokeThrown",
            "allianceFlagCaptured", "buildingsDestroyed", "buildingsLost", "buildingsExplosiveDestroyed",
            "trapsTriggered", "trapDisarmTriggered",
            "cashFound", "woodFound", "metalFound", "clothFound", "foodFound", "waterFound",
            "ammunitionFound", "ammunitionUsed",
            "weaponsFound", "gearFound", "junkFound", "medicalFound", "craftingFound",
            "researchFound", "researchNoteFound", "clothingFound", "cratesFound", "schematicsFound",
            "effectFound", "rareWeaponFound", "rareGearFound", "uniqueWeaponFound", "uniqueGearFound",
            "greyWeaponFound", "greyGearFound", "whiteWeaponFound", "whiteGearFound",
            "greenWeaponFound", "greenGearFound", "blueWeaponFound", "blueGearFound",
            "purpleWeaponFound", "purpleGearFound", "premiumWeaponFound", "premiumGearFound"
        )

        val killData = buildMap {
            for ((kAny, v) in m) {
                val k = kAny?.toString() ?: continue
                if (k.endsWith("-kills") || k.endsWith("-explosive-kills")) {
                    put(k, asInt(v))
                }
            }
        }

        val customData = buildMap {
            for ((kAny, v) in m) {
                val k = kAny?.toString() ?: continue
                if (k !in knownKeys && !k.endsWith("-kills") && !k.endsWith("-explosive-kills")) {
                    val iv = asInt(v)
                    if (iv != 0) put(k, iv)
                }
            }
        }

        return MissionStats(
            zombieSpawned = asInt(m["zombieSpawned"]),
            levelUps = asInt(m["levelUps"]),
            damageOutput = asDouble(m["damageOutput"]),
            damageTaken = asDouble(m["damageTaken"]),
            containersSearched = asInt(m["containersSearched"]),
            survivorKills = asInt(m["survivorKills"]),
            survivorsDowned = asInt(m["survivorsDowned"]),
            survivorExplosiveKills = asInt(m["survivorExplosiveKills"]),
            humanKills = asInt(m["humanKills"]),
            humanExplosiveKills = asInt(m["humanExplosiveKills"]),
            zombieKills = asInt(m["zombieKills"]),
            zombieExplosiveKills = asInt(m["zombieExplosiveKills"]),
            hpHealed = asInt(m["hpHealed"]),
            explosivesPlaced = asInt(m["explosivesPlaced"]),
            grenadesThrown = asInt(m["grenadesThrown"]),
            grenadesSmokeThrown = asInt(m["grenadesSmokeThrown"]),
            allianceFlagCaptured = asInt(m["allianceFlagCaptured"]),
            buildingsDestroyed = asInt(m["buildingsDestroyed"]),
            buildingsLost = asInt(m["buildingsLost"]),
            buildingsExplosiveDestroyed = asInt(m["buildingsExplosiveDestroyed"]),
            trapsTriggered = asInt(m["trapsTriggered"]),
            trapDisarmTriggered = asInt(m["trapDisarmTriggered"]),
            cashFound = asInt(m["cashFound"]),
            woodFound = asInt(m["woodFound"]),
            metalFound = asInt(m["metalFound"]),
            clothFound = asInt(m["clothFound"]),
            foodFound = asInt(m["foodFound"]),
            waterFound = asInt(m["waterFound"]),
            ammunitionFound = asInt(m["ammunitionFound"]),
            ammunitionUsed = asInt(m["ammunitionUsed"]),
            weaponsFound = asInt(m["weaponsFound"]),
            gearFound = asInt(m["gearFound"]),
            junkFound = asInt(m["junkFound"]),
            medicalFound = asInt(m["medicalFound"]),
            craftingFound = asInt(m["craftingFound"]),
            researchFound = asInt(m["researchFound"]),
            researchNoteFound = asInt(m["researchNoteFound"]),
            clothingFound = asInt(m["clothingFound"]),
            cratesFound = asInt(m["cratesFound"]),
            schematicsFound = asInt(m["schematicsFound"]),
            effectFound = asInt(m["effectFound"]),
            rareWeaponFound = asInt(m["rareWeaponFound"]),
            rareGearFound = asInt(m["rareGearFound"]),
            uniqueWeaponFound = asInt(m["uniqueWeaponFound"]),
            uniqueGearFound = asInt(m["uniqueGearFound"]),
            greyWeaponFound = asInt(m["greyWeaponFound"]),
            greyGearFound = asInt(m["greyGearFound"]),
            whiteWeaponFound = asInt(m["whiteWeaponFound"]),
            whiteGearFound = asInt(m["whiteGearFound"]),
            greenWeaponFound = asInt(m["greenWeaponFound"]),
            greenGearFound = asInt(m["greenGearFound"]),
            blueWeaponFound = asInt(m["blueWeaponFound"]),
            blueGearFound = asInt(m["blueGearFound"]),
            purpleWeaponFound = asInt(m["purpleWeaponFound"]),
            purpleGearFound = asInt(m["purpleGearFound"]),
            premiumWeaponFound = asInt(m["premiumWeaponFound"]),
            premiumGearFound = asInt(m["premiumGearFound"]),
            killData = killData,
            customData = customData
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun summarizeLoots(data: Map<String, Any?>, serverInsertedLoots: List<LootContent>): List<Item> {
        val lootedIds: List<String> =
            requireNotNull(data["loot"] as? List<String>) { "Error: 'loot' structure in data is not as expected, data: $data" }
        val items = mutableSetOf<Item>()

        lootedIds.forEach { lootId ->
            val loot = serverInsertedLoots.find { it.lootId == lootId }
            if (loot != null) {
                items.add(
                    Item(
                        id = Ids.uuid(),
                        type = loot.itemIdInXML,
                        qty = loot.quantity.toUInt(),
                        new = true
                    )
                )
            } else {
                Fancam.warn { "Unexpected scenario: player reportedly loot:$lootId but it doesn't exist in serverInsertedLoots." }
            }
        }

        return items.toList()
    }

    private fun buildInventoryAndResource(items: List<Item>): Pair<List<Item>, GameResources> {
        var totalRes = GameResources()
        val def = GameReference.get<ItemsDefinition>()

        for (item in items) {
            if (def.isResourceItem(item.type)) {
                val resAmount = def.getResourceAmount(item.type)
                if (resAmount != null) {
                    totalRes += resAmount
                } else {
                    Fancam.warn {
                        "Unexpected scenario: item=${item.compactString()} was classified as resource item but getResourceAmount returns null"
                    }
                }
            }
        }

        return items.stackOwnItems(GameReference) to totalRes
    }

    private fun calculateNewLevelAndPoints(currentLevel: Int, currentXp: Int, newXp: Int): Pair<Int, Int> {
        var level = currentLevel
        var levelPts = 0
        var xp = currentXp

        while (xp < newXp) {
            val xpForNextLevel = calculateXpForNextLevel(level)
            if (newXp >= xp + xpForNextLevel) {
                level++
                levelPts++
                xp += xpForNextLevel
            } else {
                break
            }
        }

        return Pair(level, levelPts)
    }

    private fun calculateXpForNextLevel(currentLevel: Int): Int {
        return (100 * (currentLevel.toDouble().pow(1.5))).toInt()
    }

    private fun calculateMissionXp(killData: Map<String, Int>): Int {
        var totalXp = 50
        totalXp += (killData["zombie"] ?: 0) * 5
        totalXp += (killData["runner"] ?: 0) * 10
        totalXp += (killData["fatty"] ?: 0) * 15
        totalXp += (killData["boss"] ?: 0) * 100
        return totalXp.coerceAtMost(1000)
    }
}
