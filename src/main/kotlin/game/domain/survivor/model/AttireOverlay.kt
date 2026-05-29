package game.domain.survivor.model

import kotlinx.serialization.Serializable

@Serializable
data class AttireOverlay(
    val type: String,
    val texture: String
)
