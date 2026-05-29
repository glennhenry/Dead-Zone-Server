package game.domain.model.game.data.alliance

import kotlinx.serialization.Serializable
import game.domain.model.game.data.effects.Effect

@Serializable
data class AllianceRound(
    val roundNum: Int?,
    val roundActive: Long?,
    val roundEnd: Long?,
    val roundEffects: List<Effect>?,
    val roundMembers: Int?
)
