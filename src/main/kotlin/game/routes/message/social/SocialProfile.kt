package game.routes.message.social

import game.Globals
import io.ktor.util.date.getTimeMillis
import kotlinx.serialization.Serializable

@Serializable
data class SocialProfile(
    val userId: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val lastOnline: Long = 0,
    val countryCode: String = "",
    val userToken: String = "",
) {
    companion object {
        fun admin(): SocialProfile {
            return SocialProfile(
                userId = Globals.ADMIN_PLAYER_ID,
                displayName = Globals.ADMIN_USERNAME,
                avatarUrl = "http://picsum.photos/200/300",
                lastOnline = getTimeMillis() - (1000L * 2000),
                countryCode = "en",
                userToken = Globals.ADMIN_TOKEN,
            )
        }
    }
}
