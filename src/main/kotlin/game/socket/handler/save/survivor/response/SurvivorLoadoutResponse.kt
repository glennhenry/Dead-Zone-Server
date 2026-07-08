package game.socket.handler.save.survivor.response

import game.socket.handler.save.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class SurvivorLoadoutResponse(
    val success: Boolean = true,
    val bind: List<String>? = null
): BaseResponse()
