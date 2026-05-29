package game.domain.model.game.data.alliance

import encore.datastore.collection.PlayerId
import kotlinx.serialization.Serializable

@Serializable
data class AllianceMessage(
    val id: String,
    val date: Long,
    val playerId: PlayerId,
    val author: String,
    val title: String,
    val message: String
)
