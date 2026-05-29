package game.domain.model.data.user

import kotlinx.serialization.Serializable

@Serializable
data class AbstractUser(
    val data: UserData,
    val email: String,
    val time: Long,
    val defaultCurrency: String // Currency constants
)
