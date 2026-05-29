package game.routes.rpc

import encore.context.ServerContext
import encore.utils.types.okOrThrow
import game.Globals
import game.routes.message.social.SocialProfile
import game.routes.message.social.SocialRefreshOutput
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * SocialRefresh (API 601)
 *
 * Input: `SocialRefreshArgs` (empty)
 *
 * Output: `SocialRefreshOutput`
 */
@OptIn(ExperimentalSerializationApi::class)
suspend fun RoutingContext.socialRefresh(serverContext: ServerContext, token: String) {
    val socialRefreshArgs = call.receiveChannel().toByteArray() // Actually no input is given

    // social features not implemented yet
    // likely we shouldn't bother with PIO publishing network and instead implement ourselves
    val pid = serverContext.subunits.session.getUserId(token)!!
    val profile = serverContext.subunits.account.getProfile(pid).okOrThrow() ?: return

    val socialRefreshOutput = if (pid == Globals.ADMIN_PLAYER_ID) {
        SocialRefreshOutput.admin()
    } else {
        SocialRefreshOutput(
            myProfile = SocialProfile(
                userId = pid,
                displayName = profile.displayName,
                avatarUrl = profile.avatarUrl,
                lastOnline = profile.lastLogin,
                countryCode = profile.countryCode ?: "",
                userToken = token,
            ),
            friends = emptyList(),
            blocked = ""
        )
    }

    val encodedOutput = ProtoBuf.encodeToByteArray<SocialRefreshOutput>(socialRefreshOutput)

    call.respondBytes(encodedOutput.pioFraming())
}
