package game.socket.handler.save.survivor

import encore.context.ServerContext
import encore.context.requirePlayerContext
import encore.fancam.Fancam
import encore.network.transport.Connection
import encore.serialization.JSON
import encore.utils.types.isOk
import game.domain.metadata.model.PlayerFlags
import game.domain.survivor.model.HumanAppearance
import game.domain.survivor.model.SurvivorClassConstants_Constants
import game.domain.survivor.model.SurvivorLoadoutEntry
import game.socket.handler.buildMsg
import game.socket.handler.save.SaveSubHandler
import game.socket.handler.save.survivor.response.PlayerCustomResponse
import game.socket.handler.save.survivor.response.SurvivorClassResponse
import game.socket.handler.save.survivor.response.SurvivorEditResponse
import game.socket.handler.save.survivor.response.SurvivorLoadoutResponse
import game.socket.handler.save.survivor.response.SurvivorRenameResponse
import game.socket.protocol.PIOSerializer
import game.socket.protocol.SaveDataMethod
import kotlin.text.get

class SurvivorSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.SURVIVOR_SAVES

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
            SaveDataMethod.SURVIVOR_CLASS -> {
                val survivorId = data["survivorId"] as? String
                val classId = data["classId"] as? String

                if (survivorId == null || classId == null) {
                    val responseJson = JSON.encode(
                        SurvivorClassResponse(success = false, error = "invalid_params")
                    )
                    send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
                    return
                }

                val validClasses = listOf(
                    SurvivorClassConstants_Constants.FIGHTER.value,
                    SurvivorClassConstants_Constants.MEDIC.value,
                    SurvivorClassConstants_Constants.SCAVENGER.value,
                    SurvivorClassConstants_Constants.ENGINEER.value,
                    SurvivorClassConstants_Constants.RECON.value,
                    SurvivorClassConstants_Constants.UNASSIGNED.value
                )

                if (classId !in validClasses) {
                    val responseJson = JSON.encode(
                        SurvivorClassResponse(success = false, error = "invalid_class")
                    )
                    send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
                    return
                }

                val svc = serverContext.requirePlayerContext(playerId).subunits

                val updateReport = svc.survivor.updateSurvivor(srvId = survivorId) { currentSurvivor ->
                    currentSurvivor.copy(classId = classId)
                }

                val responseJson = if (updateReport.isOk()) {
                    JSON.encode(SurvivorClassResponse(success = true))
                } else {
                    Fancam.error(tag = "srv_save") { "Failed to update survivor class to $classId" }
                    JSON.encode(
                        SurvivorClassResponse(success = false, error = "update_failed")
                    )
                }
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.SURVIVOR_OFFENCE_LOADOUT -> {
                val loadoutDataList = (data as? List<*>) ?: (data["data"] as? List<*>)

                if (loadoutDataList == null) {
                    val responseJson = JSON.encode(
                        SurvivorLoadoutResponse(success = false)
                    )
                    send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
                    return
                }

                val updatedLoadouts = mutableMapOf<String, SurvivorLoadoutEntry>()
                val bindItemIds = mutableListOf<String>()

                for (loadoutData in loadoutDataList) {
                    val loadoutMap = loadoutData as? Map<*, *> ?: continue
                    val survivorId = loadoutMap["id"] as? String ?: continue
                    val weaponId = (loadoutMap["weapon"] ?: loadoutMap["w"] ?: "") as? String ?: ""
                    val gear1Id = (loadoutMap["gearPassive"] ?: loadoutMap["g1"] ?: "") as? String ?: ""
                    val gear2Id = (loadoutMap["gearActive"] ?: loadoutMap["g2"] ?: "") as? String ?: ""

                    updatedLoadouts[survivorId] = SurvivorLoadoutEntry(
                        weapon = weaponId,
                        gear1 = gear1Id,
                        gear2 = gear2Id
                    )

                    if (weaponId.isNotEmpty()) bindItemIds.add(weaponId)
                    if (gear1Id.isNotEmpty()) bindItemIds.add(gear1Id)
                    if (gear2Id.isNotEmpty()) bindItemIds.add(gear2Id)
                }

                // temp TODO()
                serverContext.dataStore.updatePlayerObjectsField(playerId, "offenceLoadout", updatedLoadouts)
                val responseJson = JSON.encode(
                    SurvivorLoadoutResponse(success = true, bind = bindItemIds)
                )
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.SURVIVOR_DEFENCE_LOADOUT -> {
                val loadoutDataList = (data as? List<*>) ?: (data["data"] as? List<*>)

                if (loadoutDataList == null) {
                    val responseJson = JSON.encode(
                        SurvivorLoadoutResponse(success = false)
                    )
                    send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
                    return
                }

                val updatedLoadouts = mutableMapOf<String, SurvivorLoadoutEntry>()
                val bindItemIds = mutableListOf<String>()

                for (loadoutData in loadoutDataList) {
                    val loadoutMap = loadoutData as? Map<*, *> ?: continue
                    val survivorId = loadoutMap["id"] as? String ?: continue
                    val weaponId = (loadoutMap["weapon"] ?: loadoutMap["w"] ?: "") as? String ?: ""
                    val gear1Id = (loadoutMap["gearPassive"] ?: loadoutMap["g1"] ?: "") as? String ?: ""
                    val gear2Id = (loadoutMap["gearActive"] ?: loadoutMap["g2"] ?: "") as? String ?: ""

                    updatedLoadouts[survivorId] = SurvivorLoadoutEntry(
                        weapon = weaponId,
                        gear1 = gear1Id,
                        gear2 = gear2Id
                    )

                    if (weaponId.isNotEmpty()) bindItemIds.add(weaponId)
                    if (gear1Id.isNotEmpty()) bindItemIds.add(gear1Id)
                    if (gear2Id.isNotEmpty()) bindItemIds.add(gear2Id)
                }

                // temp TODO()
                serverContext.dataStore.updatePlayerObjectsField(playerId, "defenceLoadout", updatedLoadouts)
                val responseJson = JSON.encode(
                    SurvivorLoadoutResponse(success = true, bind = bindItemIds)
                )
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.SURVIVOR_CLOTHING_LOADOUT -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_CLOTHING_LOADOUT' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_INJURY_SPEED_UP -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_INJURY_SPEED_UP' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_RENAME -> {
                val survivorId = data["id"] as? String
                val name = data["name"] as? String

                if (survivorId == null || name == null) {
                    val responseJson = JSON.encode(
                        SurvivorRenameResponse(success = false, error = "name_invalid")
                    )
                    send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
                    return
                }

                val trimmedName = name.trim()

                if (trimmedName.length < 3) {
                    val responseJson = JSON.encode(
                        SurvivorRenameResponse(success = false, error = "name_short")
                    )
                    send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
                    return
                }

                if (trimmedName.length > 30) {
                    val responseJson = JSON.encode(
                        SurvivorRenameResponse(success = false, error = "name_long")
                    )
                    send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
                    return
                }

                if (!trimmedName.matches(Regex("^[a-zA-Z0-9 ]+$"))) {
                    val responseJson = JSON.encode(
                        SurvivorRenameResponse(success = false, error = "name_invalid")
                    )
                    send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
                    return
                }

                val svc = serverContext.requirePlayerContext(playerId).subunits

                val updateResult = svc.survivor.updateSurvivor(srvId = survivorId) { currentSurvivor ->
                    currentSurvivor.copy(
                        title = trimmedName,
                        firstName = trimmedName.split(" ").firstOrNull() ?: trimmedName,
                        lastName = trimmedName.split(" ").getOrNull(1) ?: ""
                    )
                }

                val responseJson = if (updateResult.isOk()) {
                    JSON.encode(
                        SurvivorRenameResponse(success = true, name = trimmedName, id = survivorId)
                    )
                } else {
                    Fancam.error(tag = "srv_save") { "Failed to rename survivor $name for srvId=$survivorId" }
                    JSON.encode(
                        SurvivorRenameResponse(success = false, error = "name_invalid")
                    )
                }
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.SURVIVOR_REASSIGN -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_REASSIGN' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_REASSIGN_SPEED_UP -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_REASSIGN_SPEED_UP' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_BUY -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_BUY' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_INJURE -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_INJURE' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_ENEMY_INJURE -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_ENEMY_INJURE' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_HEAL_INJURY -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_HEAL_INJURY' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_HEAL_ALL -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_HEAL_ALL' message [not implemented]" }
            }

            SaveDataMethod.PLAYER_CUSTOM -> {
                val ap = data["ap"] as? Map<*, *> ?: return
                val title = data["name"] as? String ?: return
                val voice = data["v"] as? String ?: return
                val gender = data["g"] as? String ?: return
                val appearance = HumanAppearance.parse(ap)
                @Suppress("SENSELESS_COMPARISON")
                if (appearance == null) {
                    Fancam.error(tag = "save") { "Failed to parse rawappearance=$ap" }
                    return
                }

                val bannedNicknames = listOf("dick")
                val nicknameNotAllowed = bannedNicknames.any { bannedWord ->
                    title.contains(bannedWord)
                }
                if (nicknameNotAllowed) {
                    val responseJson = JSON.encode(
                        PlayerCustomResponse(error = "Nickname not allowed")
                    )
                    send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
                    return
                }

                val svc = serverContext.requirePlayerContext(playerId).subunits

                // TODO respond to DB failure
                svc.playerObjectMetadata.updatePlayerFlags(
                    flags = PlayerFlags.create(nicknameVerified = true)
                )

                // TODO respond to DB failure
                svc.playerObjectMetadata.updatePlayerNickname(nickname = title)

                // TODO respond to DB failure
                svc.survivor.updateSurvivor(srvId = svc.survivor.survivorLeaderId) {
                    svc.survivor.getSurvivorLeader().copy(
                        title = title,
                        firstName = title.split(" ").firstOrNull() ?: "",
                        lastName = title.split(" ").getOrNull(1) ?: "",
                        voice = voice,
                        gender = gender,
                        appearance = appearance
                    )
                }

                val responseJson = JSON.encode(PlayerCustomResponse())

                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.SURVIVOR_EDIT -> {
                val survivorId = data["id"] as? String ?: return
                val ap = data["ap"] as? Map<*, *>
                val gender = data["g"] as? String
                val voice = data["v"] as? String

                Fancam.info(tag = "srv_save") { "Editing survivor id=$survivorId, ap=$ap, gender=$gender, voice=$voice" }

                val svc = serverContext.requirePlayerContext(playerId).subunits

                // TODO respond to DB failure
                svc.survivor.updateSurvivor(srvId = survivorId) { currentSurvivor ->
                    var updatedSurvivor = currentSurvivor

                    if (ap != null) {
                        val appearance = HumanAppearance.parse(ap)
                        @Suppress("SENSELESS_COMPARISON")
                        if (appearance != null) {
                            updatedSurvivor = updatedSurvivor.copy(appearance = appearance)
                        } else {
                            Fancam.error(tag = "srv_save") { "Failed to parse appearance=$ap" }
                        }
                    }

                    if (gender != null) {
                        updatedSurvivor = updatedSurvivor.copy(gender = gender)
                    }

                    if (voice != null) {
                        updatedSurvivor = updatedSurvivor.copy(voice = voice)
                    }

                    updatedSurvivor
                }

                val responseJson = JSON.encode(SurvivorEditResponse(success = true))
                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.NAMES -> {
                Fancam.warn(tag = "save") { "Received 'NAMES' message [not implemented]" }
            }

            SaveDataMethod.RESET_LEADER -> {
                Fancam.warn(tag = "save") { "Received 'RESET_LEADER' message [not implemented]" }
            }
        }
    }
}
