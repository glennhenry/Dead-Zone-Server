package game.routes.rpc

import encore.context.ServerContext
import encore.fancam.Fancam
import encore.serialization.Protobuf
import encore.venue.Venue
import game.Globals
import game.routes.message.auth.AuthenticateArgs
import game.routes.message.auth.AuthenticateOutput
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Authenticate (API 13)
 *
 * Input: `AuthenticateArgs`
 *
 * Output: `AuthenticateOutput`
 */
@OptIn(ExperimentalSerializationApi::class)
suspend fun RoutingContext.authenticate(serverContext: ServerContext) {
    val authenticateArgs = Protobuf.decode<AuthenticateArgs>(
        call.receiveChannel().toByteArray()
    )

    val userToken = authenticateArgs
        .authenticationArguments
        .find { it.key == "userToken" }?.value

    if (userToken == null) {
        Fancam.error { "Missing userToken in API 13 request" }
        call.respond(HttpStatusCode.BadRequest, "userToken is missing")
        return
    }

    val authenticateOutput = if (userToken == Globals.ADMIN_TOKEN) {
        Fancam.info { "auth by admin" }
        AuthenticateOutput.admin()
    } else {
        val isValidToken = serverContext.subunits.session.verify(userToken)
        if (isValidToken) {
            AuthenticateOutput(
                token = userToken,
                userId = serverContext.subunits.session.getUserId(userToken)!!,
                apiServerHosts = listOf(Venue.encore.server.host)
            )
        } else {
            call.respond(HttpStatusCode.Unauthorized, "token is invalid")
            null
        }
    } ?: return

    val encodedOutput = ProtoBuf.encodeToByteArray<AuthenticateOutput>(
        authenticateOutput
    )

    call.respondBytes(encodedOutput.pioFraming())
}
