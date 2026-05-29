package game.domain.model.game.data

import kotlinx.serialization.Serializable

@Serializable
data class BatchRecycleJobCollection(
    val list: List<BatchRecycleJob> = listOf()
)
