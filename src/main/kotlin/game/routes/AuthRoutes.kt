package game.routes

import encore.auth.LoginResult
import encore.context.ServerContext
import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard.NoAuthGuard
import encore.route.handle
import encore.utils.types.okOrThrow
import encore.venue.Venue
import game.Globals
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlin.text.isNullOrBlank

class AuthRoutes(private val serverContext: ServerContext) : RouteHandler {
    override fun Route.install() {
        post("/api/login") {
            handle(call, NoAuthGuard) {
                val data = call.receive<Map<String, String>>()
                val username = data["username"]
                val password = data["password"]

                if (username == null || password == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("reason" to "Missing credentials"))
                    return@handle
                }

                if (username == Globals.ADMIN_RESERVED_NAME) {
                    if (Venue.encore.adminEnabled) {
                        val session = serverContext.subunits.auth.loginAsAdmin()
                        if (session != null) {
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("playerId" to session.userId, "token" to session.token)
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                mapOf("reason" to "unexpected error: admin account doesn't exist")
                            )
                        }
                    } else {
                        call.respond(HttpStatusCode.Forbidden, mapOf("reason" to "admin account not enabled"))
                    }
                    return@handle
                }

                val usernameExist = serverContext.subunits.auth.isUsernameAvailable(username)
                val outcome = usernameExist.okOrThrow()

                if (outcome) {
                    when (val result = serverContext.subunits.auth.login(username, password).okOrThrow()) {
                        is LoginResult.AccountNotFound -> {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                mapOf("reason" to "account not found")
                            )
                        }

                        is LoginResult.InvalidCredentials -> {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                mapOf("reason" to "wrong password")
                            )
                        }

                        is LoginResult.Success -> {
                            call.respond(
                                HttpStatusCode.OK,
                                mapOf("playerId" to result.session.userId, "token" to result.session.token)
                            )
                        }
                    }
                } else {
                    val result = serverContext.subunits.auth.register(username, password).okOrThrow()
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("playerId" to result.userId, "token" to result.token)
                    )
                }
            }
        }

        get("/api/userexist") {
            handle(call, NoAuthGuard) {
                val username = call.parameters["username"]
                if (username.isNullOrBlank()) {
                    call.respondText("no", status = HttpStatusCode.BadRequest)
                    return@handle
                }

                if (username == Globals.ADMIN_RESERVED_NAME) {
                    if (Venue.encore.adminEnabled) {
                        call.respondText("granted")
                    } else {
                        call.respondText("reserved")
                    }
                    return@handle
                }

                try {
                    val exists = serverContext.subunits.auth.isUsernameAvailable(username).okOrThrow()
                    call.respondText(if (exists) "yes" else "no")
                } catch (e: Exception) {
                    Fancam.error { "Failed to check if user exists: $username, e.message:${e.message}" }
                    call.respond(HttpStatusCode.InternalServerError, mapOf("reason" to "Database error"))
                }
            }
        }

        get("/keepalive") {
            handle(call, NoAuthGuard) {
                val token =
                    call.parameters["token"] ?: return@handle call.respond(HttpStatusCode.BadRequest, "missing token")
                if (serverContext.subunits.session.refresh(token)) {
                    return@handle call.respond(HttpStatusCode.OK)
                } else {
                    return@handle call.respond(HttpStatusCode.Unauthorized, "Session expired, please login again")
                }
            }
        }
    }
}
