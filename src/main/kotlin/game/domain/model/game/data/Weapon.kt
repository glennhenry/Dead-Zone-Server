package game.domain.model.game.data

import kotlinx.serialization.Serializable
import game.domain.survivor.model.injury.InjuryCause

@Serializable
data class Weapon(
    val attachments: List<String>,
    val burstFire: Boolean,
    val injuryCause: game.domain.survivor.model.injury.InjuryCause,
    val weaponClass: WeaponClass,
    val animType: String,
    val reloadAnim: String,
    val swingAnims: List<String>,
    val playSwingExertionSound: Boolean = true,
    val flags: UInt = 0u,
    val weaponType: UInt = 0u,
    val ammoType: UInt = 0u,
    val survivorClasses: List<String> = listOf()
)
