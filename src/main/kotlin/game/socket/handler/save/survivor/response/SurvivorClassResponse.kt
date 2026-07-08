package game.socket.handler.save.survivor.response

import game.socket.handler.save.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class SurvivorClassResponse(
    val success: Boolean = true,
    val error: String? = null
): BaseResponse()
