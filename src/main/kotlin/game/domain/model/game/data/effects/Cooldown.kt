package game.domain.model.game.data.effects

import game.domain.model.game.data.TimerData
import kotlinx.serialization.Serializable

@Serializable
data class Cooldown(
    val raw: ByteArray,  // see readObject of Cooldown
    val id: String,
    val type: UInt,
    val subType: String,
    val timer: TimerData
)
