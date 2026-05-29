package game.socket.handler.save.command

import encore.context.ServerContext
import encore.fancam.Fancam
import encore.network.transport.Connection
import encore.serialization.JSON
import encore.utils.identifier.Ids
import game.socket.handler.buildMsg
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.CommandMessage
import game.socket.protocol.PIOSerializer

class CommandSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = CommandMessage.COMMAND_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            CommandMessage.GIVE -> {
                val type = data["type"] as? String ?: return

                Fancam.info(tag = "save") { "Received 'give' command with type=$type | data=$data" }

                when (type) {
                    "schematic" -> {
                        // not tested
                        val schem = data["schem"] as? String ?: return
                        val item = game.domain.items.model.SchematicItem(
                            type = type,
                            schem = schem,
                            new = true
                        )
                        val response = JSON.encode(item)
                        send(PIOSerializer.serialize(buildMsg(saveId, response)))
                    }

                    "crate" -> {
                        // not tested
                        val series = data["series"] as? Int ?: return
                        val repeat = (data["repeat"] as? Int) ?: 1
                        repeat(repeat) {
                            val item = game.domain.items.model.CrateItem(
                                type = type,
                                series = series,
                                new = true
                            )
                            val response = JSON.encode(item)
                            send(PIOSerializer.serialize(buildMsg(saveId, response)))
                        }
                    }

                    "effect" -> {
                        Fancam.warn(tag = "save") { "Received 'give' command of type effect [not implemented]" }
                    }

                    else -> {
                        // not tested with mod
                        val level = data["level"] as? Int ?: return
                        val qty = data["qty"] as? Int ?: 1
                        val mod1 = data["mod1"] as? String?
                        val mod2 = data["mod2"] as? String?
                        val item = game.domain.items.model.Item(
                            id = Ids.uuid(),
                            type = type,
                            level = level,
                            qty = qty.toUInt(),
                            mod1 = mod1,
                            mod2 = mod2,
                            new = true,
                        )
                        val response = JSON.encode(item)
                        send(PIOSerializer.serialize(buildMsg(saveId, response)))
                    }
                }
            }


            CommandMessage.GIVE_RARE -> {
                val type = (data["type"] as String?) ?: return
                val level = (data["level"] as Int?) ?: return

                val item = game.domain.items.model.Item(
                    id = Ids.uuid(),
                    type = type,
                    level = level,
                    quality = 50,
                    new = true,
                )

                Fancam.info(tag = "save") { "Received 'giveRare' command with type=$type | level=$level" }

                val response = JSON.encode(item)
                send(PIOSerializer.serialize(buildMsg(saveId, response)))
            }

            CommandMessage.GIVE_UNIQUE -> {
                val type = (data["type"] as String?) ?: return
                val level = (data["level"] as Int?) ?: return

                val item = game.domain.items.model.Item(
                    id = Ids.uuid(),
                    type = type,
                    level = level,
                    quality = 51,
                    new = true,
                )

                Fancam.info(tag = "save") { "Received 'giveUnique' command with type=$type | level=$level" }

                val response = JSON.encode(item)
                send(PIOSerializer.serialize(buildMsg(saveId, response)))
            }

            CommandMessage.STORE_CLEAR -> {
                Fancam.warn(tag = "save") { "Received 'STORE_CLEAR' message [not implemented]" }
            }

            CommandMessage.STORE_BLOCK -> {
                Fancam.warn(tag = "save") { "Received 'STORE_BLOCK' message [not implemented]" }
            }

            CommandMessage.SPAWN_ELITE -> {
                Fancam.warn(tag = "save") { "Received 'SPAWN_ELITE' message [not implemented]" }
            }

            CommandMessage.ELITE_CHANCE -> {
                Fancam.warn(tag = "save") { "Received 'ELITE_CHANCE' message [not implemented]" }
            }

            CommandMessage.ADD_BOUNTY -> {
                Fancam.warn(tag = "save") { "Received 'ADD_BOUNTY' message [not implemented]" }
            }

            CommandMessage.LEVEL -> {
                Fancam.warn(tag = "save") { "Received 'LEVEL' message [not implemented]" }
            }

            CommandMessage.SERVER_TIME -> {
                Fancam.warn(tag = "save") { "Received 'SERVER_TIME' message [not implemented]" }
            }

            CommandMessage.ZOMBIE -> {
                Fancam.warn(tag = "save") { "Received 'ZOMBIE' message [not implemented]" }
            }

            CommandMessage.TIME -> {
                Fancam.warn(tag = "save") { "Received 'TIME' message [not implemented]" }
            }

            CommandMessage.STAT -> {
                Fancam.warn(tag = "save") { "Received 'STAT' message [not implemented]" }
            }

            CommandMessage.GIVE_AMOUNT -> {
                Fancam.warn(tag = "save") { "Received 'GIVE_AMOUNT' message [not implemented]" }
            }

            CommandMessage.COUNTER -> {
                Fancam.warn(tag = "save") { "Received 'COUNTER' message [not implemented]" }
            }

            CommandMessage.DAILY_QUEST -> {
                Fancam.warn(tag = "save") { "Received 'DAILY_QUEST' message [not implemented]" }
            }

            CommandMessage.CHAT -> {
                Fancam.warn(tag = "save") { "Received 'CHAT' message [not implemented]" }
            }

            CommandMessage.LANG -> {
                Fancam.warn(tag = "save") { "Received 'LANG' message [not implemented]" }
            }

            CommandMessage.FLAG -> {
                Fancam.warn(tag = "save") { "Received 'FLAG' message [not implemented]" }
            }

            CommandMessage.PROMO -> {
                Fancam.warn(tag = "save") { "Received 'PROMO' message [not implemented]" }
            }

            CommandMessage.BOUNTY_ADD -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_ADD' message [not implemented]" }
            }

            CommandMessage.GIVE_INFECTED_BOUNTY -> {
                Fancam.warn(tag = "save") { "Received 'GIVE_INFECTED_BOUNTY' message [not implemented]" }
            }

            CommandMessage.BOUNTY_ABANDON -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_ABANDON' message [not implemented]" }
            }

            CommandMessage.BOUNTY_COMPLETE -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_COMPLETE' message [not implemented]" }
            }

            CommandMessage.BOUNTY_TASK_COMPLETE -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_TASK_COMPLETE' message [not implemented]" }
            }

            CommandMessage.BOUNTY_CONDITION_COMPLETE -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_CONDITION_COMPLETE' message [not implemented]" }
            }

            CommandMessage.BOUNTY_KILL -> {
                Fancam.warn(tag = "save") { "Received 'BOUNTY_KILL' message [not implemented]" }
            }

            CommandMessage.SKILL_GIVEXP -> {
                Fancam.warn(tag = "save") { "Received 'SKILL_GIVEXP' message [not implemented]" }
            }

            CommandMessage.SKILL_LEVEL -> {
                Fancam.warn(tag = "save") { "Received 'SKILL_LEVEL' message [not implemented]" }
            }
        }
    }
}
