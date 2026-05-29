package game.domain.survivor.model

import kotlinx.serialization.Serializable
import game.domain.model.game.data.WeaponClass
import game.domain.model.game.data.WeaponType

@Serializable
data class SurvivorClassWeapons(
    val classes: List<WeaponClass> = listOf(),
    val types: List<WeaponType> = listOf()
)
