package game.socket.handler.save.survivor.response

import game.socket.handler.save.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class SurvivorRenameResponse(
    val success: Boolean = true,
    val error: String? = null,
    val name: String? = null,
    val id: String? = null
): BaseResponse()
