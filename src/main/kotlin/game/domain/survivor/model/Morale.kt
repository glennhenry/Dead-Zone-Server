package game.domain.survivor.model

import kotlinx.serialization.Serializable

@Serializable
data class Morale(
    val maps: Map<String, Double> = mapOf()
)
