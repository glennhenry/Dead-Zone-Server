package skills

import kotlinx.serialization.Serializable

@Serializable
data class SkillState(
    val xp: Int,
    val level: Int
)
