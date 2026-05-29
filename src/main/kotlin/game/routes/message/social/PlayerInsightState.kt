package game.routes.message.social

import game.routes.message.utils.KeyValuePair
import kotlinx.serialization.Serializable

@Serializable
data class PlayerInsightState(
    val playersOnline: Int = 0,
    val segments: List<KeyValuePair> = emptyList()
)
