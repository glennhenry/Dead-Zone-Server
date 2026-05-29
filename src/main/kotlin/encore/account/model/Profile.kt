package encore.account.model

import encore.datastore.collection.PlayerId
import io.ktor.util.date.getTimeMillis
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
    val displayName: String,
    val avatarUrl: String,
    val createdAt: Long = getTimeMillis(),
    val lastLogin: Long = getTimeMillis(),
    val countryCode: String? = null,
    val friends: Set<Profile> = emptySet(),
    val enemies: Set<Profile> = emptySet(),
)
