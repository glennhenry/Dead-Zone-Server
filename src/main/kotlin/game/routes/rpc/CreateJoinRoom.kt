package game.routes.rpc

import encore.serialization.Protobuf
import game.routes.message.server.CreateJoinRoomArgs
import game.routes.message.server.CreateJoinRoomOutput
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * CreateJoinRoom (API 27)
 *
 * Input: `CreateJoinRoomArgs`
 *
 * Output: `CreateJoinRoomOutput`
 */
@OptIn(ExperimentalSerializationApi::class)
suspend fun RoutingContext.createJoinRoom() {
    val createJoinRoomArgs = Protobuf.decode<CreateJoinRoomArgs>(
        call.receiveChannel().toByteArray()
    )

    val createJoinRoomOutput = ProtoBuf.encodeToByteArray<CreateJoinRoomOutput>(
        CreateJoinRoomOutput.defaultRoom()
    )

    call.respondBytes(createJoinRoomOutput.pioFraming())
}
