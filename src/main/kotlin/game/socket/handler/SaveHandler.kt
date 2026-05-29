package game.socket.handler

import encore.context.ServerContext
import encore.fancam.Fancam
import encore.network.fanchant.FanchantType
import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import encore.time.TimeCenter
import game.socket.handler.save.arena.ArenaSaveHandler
import game.socket.handler.save.bounty.BountySaveHandler
import game.socket.handler.save.chat.ChatSaveHandler
import game.socket.handler.save.command.CommandSaveHandler
import game.socket.handler.save.compound.building.BuildingSaveHandler
import game.socket.handler.save.compound.misc.CmpMiscSaveHandler
import game.socket.handler.save.compound.task.TaskSaveHandler
import game.socket.handler.save.crate.CrateSaveHandler
import game.socket.handler.save.item.ItemSaveHandler
import game.socket.handler.save.misc.MiscSaveHandler
import game.socket.handler.save.mission.MissionSaveHandler
import game.socket.handler.save.purchase.PurchaseSaveHandler
import game.socket.handler.save.quest.QuestSaveHandler
import game.socket.handler.save.raid.RaidSaveHandler
import game.socket.handler.save.survivor.SurvivorSaveHandler
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PioFanchant
import game.socket.protocol.PioFanchantType

/**
 * Handle `save` message by:
 *
 * 1. Receive the `data`, `_type`, and `id` (save id) for the said message.
 * 2. Route the save into the corresponding handler based on `_type`.
 * 3. Handlers determine what to do based on the given `data`.
 * 4. Optionally, response back a message of type 'r' with the expected JSON payload and the given save id.
 */
class SaveHandler(private val serverContext: ServerContext) : FanchantHandler<PioFanchant> {
    override val fanchantType: FanchantType<PioFanchant> = PioFanchantType(NetworkMessage.SAVE)
    private val saveHandlers = listOf(
        ArenaSaveHandler(), BountySaveHandler(), ChatSaveHandler(),
        CommandSaveHandler(), BuildingSaveHandler(), CmpMiscSaveHandler(),
        TaskSaveHandler(), CrateSaveHandler(), ItemSaveHandler(),
        MiscSaveHandler(), MissionSaveHandler(), PurchaseSaveHandler(),
        QuestSaveHandler(), RaidSaveHandler(), SurvivorSaveHandler(),
    )

    @Suppress("UNCHECKED_CAST")
    override suspend fun handle(ctx: HandlerContext<PioFanchant>): Unit = with(ctx) {
        val body = fanchant.getMap(NetworkMessage.SAVE) ?: emptyMap()
        val data = body["data"] as? Map<String, Any?> ?: emptyMap()
        val type = data["_type"] as? String? ?: return
        val saveId = body["id"] as? String? ?: return
        val pid = requireNotNull(connection.playerId) { "Missing playerId on save message for connection=$connection" }

        // Note: the game typically send and expects JSON data for save message
        // encode JSON response to string before using PIO serialization

        var match = false
        // 15 save handlers
        saveHandlers.forEach { saveHandler ->
            // O(1) hashset check on each handlers
            if (type in saveHandler.supportedTypes) {
                match = true
                // further string matching on the type on each handler
                saveHandler.handle(
                    connection, type, saveId, data, serverContext
                ) { bytes -> connection.write(bytes) }
            }
        }

        if (!match) {
            Fancam.warn { "Handled 's' network message but unrouted for save type: $type with data=$data" }
        }
    }
}

fun buildMsg(saveId: String?, vararg payloads: Any): List<Any> {
    return buildList {
        add("r")
        add(saveId ?: "m")
        add(TimeCenter.game.nowInDouble())
        addAll(payloads)
    }
}
