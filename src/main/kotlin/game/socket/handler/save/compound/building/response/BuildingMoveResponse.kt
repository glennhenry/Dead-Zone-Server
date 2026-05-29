package game.socket.handler.save.compound.building.response

import game.socket.handler.save.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class BuildingMoveResponse(
    val success: Boolean = true,
    val x: Int,
    val y: Int,
    val r: Int,
): BaseResponse()
