package game.domain.model.game.data

import kotlinx.serialization.Serializable
import game.domain.items.model.Item

@Serializable
data class BatchRecycleJob(
    val id: String,
    val items: List<Item>,
    val start: Long,
    val end: Int
)
