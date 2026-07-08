package game.socket.handler.save.item

import encore.context.ServerContext
import encore.context.requirePlayerContext
import encore.fancam.Fancam
import encore.network.transport.Connection
import encore.utils.identifier.Ids
import game.domain.items.model.Item
import game.socket.handler.buildMsg
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.PIOSerializer
import game.socket.protocol.SaveDataMethod
import kotlin.random.Random

class ItemSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.ITEM_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.ITEM -> {
                Fancam.warn(tag = "save") { "Received 'ITEM' message [not implemented]" }
            }

            SaveDataMethod.ITEM_BUY -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_BUY' message [not implemented]" }
            }

            SaveDataMethod.ITEM_LIST -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_LIST' message [not implemented]" }
            }

            SaveDataMethod.ITEM_RECYCLE -> {
                val itemId = data["id"] as? String
                if (itemId == null) {
                    send(PIOSerializer.serialize(buildMsg(saveId, """{"success":false}""")))
                    Fancam.warn(tag = "itm_save") { "ITEM_RECYCLE: missing 'id' parameter" }
                    return
                }

                val svc = serverContext.requirePlayerContext(connection.playerId).subunits
                val inventory = svc.inventory.getInventory()
                val itemToRecycle = inventory.find { it.id == itemId }

                if (itemToRecycle == null) {
                    send(PIOSerializer.serialize(buildMsg(saveId, """{"success":false}""")))
                    Fancam.warn(tag = "itm_save") { "ITEM_RECYCLE: item not found with id=$itemId" }
                    return
                }

                val recycledItems = generateRecycleRewards(itemToRecycle.type)
                if (recycledItems.isNotEmpty()) {
                    svc.inventory.updateInventory { items ->
                        items.filter { it.id != itemId } + recycledItems
                    }
                }

                val responseJson = if (recycledItems.isNotEmpty()) {
                    val itemsJsonArray = recycledItems.joinToString(",") { item ->
                        """{"id":"${item.id}","type":"${item.type}","qty":${item.qty},"new":true}"""
                    }
                    """{"success":true,"qty":0,"items":[$itemsJsonArray]}"""
                } else {
                    """{"success":true,"qty":0}"""
                }

                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))

                Fancam.info(tag = "itm_save") {
                    "Item recycled: id=$itemId, type=${itemToRecycle.type}, rewards=${recycledItems.size} items for player ${connection.playerId}"
                }
            }

            SaveDataMethod.ITEM_DISPOSE -> {
                val itemId = data["id"] as? String
                if (itemId == null) {
                    send(PIOSerializer.serialize(buildMsg(saveId, """{"success":false}""")))
                    Fancam.warn(tag = "itm_save") { "ITEM_DISPOSE: missing 'id' parameter" }
                    return
                }

                val svc = serverContext.requirePlayerContext(connection.playerId).subunits
                val inventory = svc.inventory.getInventory()
                val itemToDispose = inventory.find { it.id == itemId }
                if (itemToDispose == null) {
                    send(PIOSerializer.serialize(buildMsg(saveId, """{"success":false}""")))
                    Fancam.warn(tag = "itm_save") { "ITEM_DISPOSE: item not found with id=$itemId" }
                    return
                }

                svc.inventory.updateInventory { items ->
                    items.filter { it.id != itemId }
                }
                Fancam.info(tag = "itm_save") { "Item disposed: id=$itemId, type=${itemToDispose.type}, qty=${itemToDispose.qty} for player ${connection.playerId}" }

                send(PIOSerializer.serialize(buildMsg(saveId, """{"success":true,"qty":0}""")))
            }

            SaveDataMethod.ITEM_CLEAR_NEW -> {
                val svc = serverContext.requirePlayerContext(connection.playerId).subunits

                svc.inventory.updateInventory { items ->
                    items.map { item -> item.copy(new = false) }
                }

                Fancam.info(tag = "itm_save") { "Cleared 'new' flag on all items for player ${connection.playerId}" }
            }

            SaveDataMethod.ITEM_BATCH_RECYCLE -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_BATCH_RECYCLE' message [not implemented]" }
            }

            SaveDataMethod.ITEM_BATCH_RECYCLE_SPEED_UP -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_BATCH_RECYCLE_SPEED_UP' message [not implemented]" }
            }

            SaveDataMethod.ITEM_BATCH_DISPOSE -> {
                Fancam.warn(tag = "save") { "Received 'ITEM_BATCH_DISPOSE' message [not implemented]" }
            }
        }
    }

    private fun generateRecycleRewards(itemType: String): List<Item> {
        val rewards = mutableListOf<Item>()

        val baseRewards = mapOf(
            "wood" to 2..5,
            "metal" to 1..3,
            "cloth" to 1..4,
            "water" to 1..2
        )

        baseRewards.forEach { (type, range) ->
            if (Random.nextDouble() < 0.3) {
                val qty = Random.nextInt(range.first, range.last + 1).toUInt()
                rewards.add(
                    Item(
                        id = Ids.uuid(),
                        type = type,
                        qty = qty,
                        new = true
                    )
                )
            }
        }

        return rewards
    }
}
