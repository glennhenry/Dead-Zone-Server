package encore.account.model

import encore.time.TimeCenter
import game.mongo.collection.PlayerId
import kotlinx.serialization.Serializable

/**
 * Player profile information.
 *
 * `Profile` contains the player's personal information in the server
 * such as country, avatar, locale, etc. It does not include game-specific
 * information like player's ranking or status.
 *
 * @property playerId Unique identifier of the player.
 * @property createdAt Epoch millis of the account creation date.
 */
@Serializable
data class Profile(
    val playerId: PlayerId,
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val createdAt: Long = TimeCenter.now(),
    val lastLogin: Long = TimeCenter.now(),
    val countryCode: String? = null,
    val friends: Set<Profile> = emptySet(),
    val enemies: Set<Profile> = emptySet(),
)
