package game.socket.handler.save.compound.building.response

import game.domain.model.game.data.TimerData
import game.socket.handler.save.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class BuildingUpgradeResponse(
    val success: Boolean = true,
    val items: Map<String, Int>? = emptyMap(),
    val timer: TimerData,
    val xp: Int = 0,
): BaseResponse()
