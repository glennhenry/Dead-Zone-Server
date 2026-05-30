package game.routes.rpc

import game.routes.message.utils.WriteErrorArgs
import game.routes.message.utils.WriteErrorError
import encore.fancam.Fancam
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * WriteError (API 50)
 *
 * Input: `WriteErrorArgs`
 *
 * Output: `WriteErrorError` (optional)
 */
@OptIn(ExperimentalSerializationApi::class)
suspend fun RoutingContext.writeError() {
    val writeErrorArgs = ProtoBuf.decodeFromByteArray<WriteErrorArgs>(
        call.receiveChannel().toByteArray()
    )

    Fancam.error(tag = "writeerror") { writeErrorArgs.toString() }

    if (writeErrorArgs.details.contains("Load Never Completed", ignoreCase = true) ||
        writeErrorArgs.details.contains("Resource not found", ignoreCase = true) ||
        writeErrorArgs.details.contains("Resource load fail", ignoreCase = true) ||
        writeErrorArgs.details.contains("2036", ignoreCase = true) ||
        writeErrorArgs.details.contains("Stream error", ignoreCase = true)
    ) {
        Fancam.error(tag = "assets error") { writeErrorArgs.details }
    }

    val loadObjectsOutput = ProtoBuf.encodeToByteArray(WriteErrorError.dummy())

    call.respondBytes(loadObjectsOutput.pioFraming())
}
