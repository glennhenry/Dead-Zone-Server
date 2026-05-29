package game.domain.model.game.data

import kotlinx.serialization.Serializable

@Serializable
data class TaskCollection(
    val list: List<Task> = listOf()
)
