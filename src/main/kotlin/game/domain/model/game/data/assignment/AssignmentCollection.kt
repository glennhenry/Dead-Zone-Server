package game.domain.model.game.data.assignment

import kotlinx.serialization.Serializable

@Serializable
data class AssignmentCollection(
    val list: List<AssignmentData> = listOf()
)
