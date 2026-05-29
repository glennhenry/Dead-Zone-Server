package game.routes

import encore.context.ServerContext
import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard.NoAuthGuard
import encore.route.handle
import game.routes.rpc.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class ApiRoutes(private val serverContext: ServerContext) : RouteHandler {
    override fun Route.install() {
        post("/api/{path}") {
            handle(call, NoAuthGuard) {
                val path = call.parameters["path"] ?: return@handle call.respond(HttpStatusCode.BadRequest)

                val playerToken = if (path != "13" && path != "50") {
                    call.request.queryParameters["playertoken"]
                        ?: call.request.headers["playertoken"]
                        ?: return@handle call.respond(HttpStatusCode.Unauthorized, "Missing playertoken")
                } else {
                    null
                }

                when (path) {
                    "13" -> authenticate(serverContext)
                    "601" -> socialRefresh(serverContext, playerToken!!)
                    "27" -> createJoinRoom()
                    "50" -> writeError()
                    "85" -> loadObjects(serverContext)
                    else -> {
                        Fancam.error { "Unimplemented API route: $path" }
                        call.respond(HttpStatusCode.NotFound, "Unimplemented API: $path")
                    }
                }
            }
        }
    }
}
