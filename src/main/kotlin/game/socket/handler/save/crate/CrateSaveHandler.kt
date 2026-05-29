package game.socket.handler.save.crate

import encore.context.ServerContext
import encore.fancam.Fancam
import encore.network.transport.Connection
import encore.serialization.JSON
import game.socket.handler.buildMsg
import game.socket.handler.save.SaveSubHandler
import game.socket.handler.save.crate.response.CrateUnlockResponse
import game.socket.protocol.PIOSerializer
import game.socket.protocol.SaveDataMethod

class CrateSaveHandler : SaveSubHandler {
    override val supportedTypes: Set<String> = SaveDataMethod.CRATE_SAVES

    override suspend fun handle(
        connection: Connection,
        type: String,
        saveId: String,
        data: Map<String, Any?>,
        serverContext: ServerContext,
        send: suspend (ByteArray) -> Unit
    ) {
        when (type) {
            SaveDataMethod.CRATE_UNLOCK -> {
                val keyId = data["keyId"] as String?
                val crateId = (data["crateId"] ?: "") as String?

                val responseJson = JSON.encode(
                    CrateUnlockResponse(
                        success = true,
                        item = game.domain.items.ItemFactory.getRandomItem(),
                        keyId = keyId,
                        crateId = crateId,
                    )
                )

                Fancam.info(tag = "save") { "Opening crateId=$crateId with keyId=$keyId" }

                send(PIOSerializer.serialize(buildMsg(saveId, responseJson)))
            }

            SaveDataMethod.CRATE_MYSTERY_UNLOCK -> {
                Fancam.warn(tag = "save") { "Received 'CRATE_MYSTERY_UNLOCK' message [not implemented]" }
            }
        }
    }
}
