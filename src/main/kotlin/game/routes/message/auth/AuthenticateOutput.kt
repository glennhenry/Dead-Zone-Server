package game.routes.message.auth

import encore.venue.Venue
import game.Globals
import game.routes.message.social.PlayerInsightState
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticateOutput(
    val token: String = "",
    val userId: String = "",
    val showBranding: Boolean = false,
    val gameFSRedirectMap: String = "",
    val playerInsightState: PlayerInsightState? = null,
    val startDialogs: List<AuthenticateStartDialog> = emptyList(),
    val isSocialNetworkUser: Boolean = false,
    val newPlayCodes: List<String> = emptyList(),
    val notificationClickPayload: String = "",
    val isInstalledByPublishingNetwork: Boolean = false,
    val deprecated1: List<String> = emptyList(),
    val apiSecurity: Int = 0, // ENUM: depends on enum definition
    val apiServerHosts: List<String> = emptyList()
) {
    companion object {
        fun admin(): AuthenticateOutput {
            return AuthenticateOutput(
                token = Globals.ADMIN_TOKEN,
                userId = Globals.ADMIN_PLAYER_ID,
                apiServerHosts = listOf(Venue.encore.server.host)
            )
        }
    }
}
