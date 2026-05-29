package game.domain.model.game.data.skills

import kotlinx.serialization.Serializable
import skills.SkillState

@Serializable
data class SkillCollection(
    val map: Map<String, SkillState>? = mapOf()
)
