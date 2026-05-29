package game.domain.survivor.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class SurvivorAppearanceConstants(val value: String)

object SurvivorAppearanceConstants_Constants {
    val SLOT_UPPER_BODY = SurvivorAppearanceConstants("clothing_upper")
    val SLOT_LOWER_BODY = SurvivorAppearanceConstants("clothing_lower")
}
