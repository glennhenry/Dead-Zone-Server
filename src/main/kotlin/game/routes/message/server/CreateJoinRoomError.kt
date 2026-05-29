package game.routes.message.server

import kotlinx.serialization.Serializable

@Serializable
data class CreateJoinRoomError(
    val message: String = "",
    val errorCode: Int = 0,
) {
    fun dummy(): CreateJoinRoomError {
        return CreateJoinRoomError(
            message = "create-join-room-error-ex",
            errorCode = 42,
        )
    }
}
