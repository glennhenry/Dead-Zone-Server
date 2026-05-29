package game.domain.mission.model

import kotlinx.serialization.Serializable

@Serializable
data class MissionCollection(
    val list: List<MissionData> = listOf()
)
