package game.routes

import encore.fancam.Fancam
import encore.route.RouteHandler
import encore.route.guard.NoAuthGuard
import encore.route.handle
import encore.serialization.JSON
import encore.websocket.WebSocketManager
import encore.websocket.WebSocketMessage
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.serialization.json.JsonNull
import java.io.File

class DebugLogRoutes(private val wsManager: WebSocketManager) : RouteHandler {
    override fun Route.install() {
        get("/debuglog") {
            handle(call, NoAuthGuard) {
                val file = File("assets/debuglog.html")
                if (file.exists()) {
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "debuglog.html not found")
                }
            }
        }

        webSocket("/debuglog") {
            handle(call, NoAuthGuard) {
                val clientId = call.parameters["clientId"]
                if (clientId == null) {
                    close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing clientId"))
                    return@handle
                }

                wsManager.addClient(clientId, this)

                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val msg = frame.readText()
                            try {
                                val wsMessage = JSON.decode<WebSocketMessage>(msg)
                                if (wsMessage.type == "close") break
                                if (wsMessage.type == "isready") {
                                    val reply = WebSocketMessage(type = "ready", payload = JsonNull)
                                    this.send(Frame.Text(JSON.encode(reply)))
                                }
                                wsManager.handleMessage(this, wsMessage)
                            } catch (e: Exception) {
                                Fancam.error(e) { "Failed to parse WS message" }
                            }
                        }
                    }
                } catch (_: CancellationException) {
                } catch (e: Exception) {
                    Fancam.error(e) { "Error in websocket for client $this" }
                } finally {
                    wsManager.removeClient(clientId)
                    Fancam.trace { "Client $this disconnected from websocket debug." }
                }
            }
        }
    }
}
