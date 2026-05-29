package game.domain.model.game.data.alliance

import kotlinx.serialization.Serializable

@Serializable
data class AllianceRank(
    val maps: Map<UInt, UInt> = mapOf()
)
