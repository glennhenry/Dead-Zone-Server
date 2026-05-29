package game.domain.survivor.model

import kotlinx.serialization.Serializable

@Serializable
data class SurvivorClass(
    val id: String,
    val maleUpper: String,
    val maleLower: String,
    val maleSkinOverlay: String?,
    val femaleUpper: String,
    val femaleLower: String,
    val femaleSkinOverlay: String?,
    val baseAttributes: Attributes,
    val levelAttributes: Attributes,
    val hideHair: Boolean = false,
    val weapons: List<SurvivorClassWeapons>
)
