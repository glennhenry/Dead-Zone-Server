package game.socket.handler.save.purchase

import encore.context.ServerContext
import game.socket.handler.save.SaveSubHandler
import game.socket.protocol.SaveDataMethod
import encore.fancam.Fancam
import encore.network.transport.Connection

class PurchaseSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.PURCHASE_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.RESOURCE_BUY -> {
                Fancam.warn(tag = "save") { "Received 'RESOURCE_BUY' message [not implemented]" }
            }

            SaveDataMethod.PROTECTION_BUY -> {
                Fancam.warn(tag = "save") { "Received 'PROTECTION_BUY' message [not implemented]" }
            }

            SaveDataMethod.PAYVAULT_BUY -> {
                Fancam.warn(tag = "save") { "Received 'PAYVAULT_BUY' message [not implemented]" }
            }

            SaveDataMethod.CLAIM_PROMO_CODE -> {
                Fancam.warn(tag = "save") { "Received 'CLAIM_PROMO_CODE' message [not implemented]" }
            }

            SaveDataMethod.BUY_PACKAGE -> {
                Fancam.warn(tag = "save") { "Received 'BUY_PACKAGE' message [not implemented]" }
            }

            SaveDataMethod.CHECK_APPLY_DIRECT_PURCHASE -> {
                Fancam.warn(tag = "save") { "Received 'CHECK_APPLY_DIRECT_PURCHASE' message [not implemented]" }
            }

            SaveDataMethod.HAS_PAYVAULT_ITEM -> {
                Fancam.warn(tag = "save") { "Received 'HAS_PAYVAULT_ITEM' message [not implemented]" }
            }

            SaveDataMethod.INCREMENT_PURCHASE_COUNT -> {
                Fancam.warn(tag = "save") { "Received 'INCREMENT_PURCHASE_COUNT' message [not implemented]" }
            }

            SaveDataMethod.DEATH_MOBILE_RENAME -> {
                Fancam.warn(tag = "save") { "Received 'DEATH_MOBILE_RENAME' message [not implemented]" }
            }
        }
    }
}
