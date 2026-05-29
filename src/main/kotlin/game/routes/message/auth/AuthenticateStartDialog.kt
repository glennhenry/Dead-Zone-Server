package game.routes.message.auth

import game.routes.message.utils.KeyValuePair
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticateStartDialog(
    val name: String = "",
    val arguments: List<KeyValuePair> = emptyList()
)
