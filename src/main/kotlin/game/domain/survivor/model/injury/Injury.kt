package game.domain.survivor.model.injury

import game.domain.model.game.data.TimerData
import kotlinx.serialization.Serializable

@Serializable
data class Injury(
    val id: String,
    val type: String,
    val location: String,
    val severity: String,
    val damage: Double,
    val morale: Double,
    val timer: TimerData?
)
