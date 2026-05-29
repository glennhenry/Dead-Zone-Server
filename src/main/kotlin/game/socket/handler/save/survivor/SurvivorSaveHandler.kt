package game.socket.handler.save.survivor

import encore.context.ServerContext
import encore.context.requirePlayerContext
import encore.fancam.Fancam
import encore.network.transport.Connection
import encore.serialization.JSON
import game.domain.metadata.model.PlayerFlags
import game.domain.survivor.model.HumanAppearance
import game.socket.handler.buildMsg
import game.socket.handler.save.SaveSubHandler
import game.socket.handler.save.survivor.response.PlayerCustomResponse
import game.socket.protocol.PIOSerializer
import game.socket.protocol.SaveDataMethod

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
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_CLASS' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_OFFENCE_LOADOUT -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_OFFENCE_LOADOUT' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_DEFENCE_LOADOUT -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_DEFENCE_LOADOUT' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_CLOTHING_LOADOUT -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_CLOTHING_LOADOUT' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_INJURY_SPEED_UP -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_INJURY_SPEED_UP' message [not implemented]" }
            }

            SaveDataMethod.SURVIVOR_RENAME -> {
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_RENAME' message [not implemented]" }
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

                svc.playerObjectMetadata.updatePlayerFlags(
                    flags = PlayerFlags.create(nicknameVerified = true)
                )

                svc.playerObjectMetadata.updatePlayerNickname(nickname = title)

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
                Fancam.warn(tag = "save") { "Received 'SURVIVOR_EDIT' message [not implemented]" }
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
