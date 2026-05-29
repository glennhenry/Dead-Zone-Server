package game.domain.model.data.user

import kotlinx.serialization.Serializable

@Serializable
data class PlayerIOUser(
    val abstractUser: AbstractUser,
    val profile: PublishingNetworkProfile
)
