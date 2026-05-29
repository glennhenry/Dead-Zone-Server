package game.socket.handler

import encore.fancam.Fancam
import encore.network.fanchant.FanchantType
import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import encore.time.TimeCenter
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.PioFanchant
import game.socket.protocol.PioFanchantType

/**
 * Handle `rsc` message by:
 *
 * 1. Sending a reponse in JSON with success set to true
 *
 */
class RequestSurvivorCheckHandler : FanchantHandler<PioFanchant> {
    override val fanchantType: FanchantType<PioFanchant> = PioFanchantType(NetworkMessage.REQUEST_SURVIVOR_CHECK)

    override suspend fun handle(ctx: HandlerContext<PioFanchant>): Unit = with(ctx) {
        val id = fanchant.getMap("rsc")?.get("id") as? String
        Fancam.debug { "Received RSC of saveId: $id" }

        val reponseMsg =
            listOf(
                NetworkMessage.SEND_RESPONSE,  // Message Type
                id ?: "m",   // id
                TimeCenter.game.nowInDouble(),   // server time
                survivorCheckJson.trimIndent() // response
            )
        
        val newSurvivorMsg =
            listOf(
                NetworkMessage.SURVIVOR_NEW,  // Message Type
                survivorNewJson.trimIndent()
            )

        connection.write(PIOSerializer.serialize(reponseMsg))
        connection.write(PIOSerializer.serialize(newSurvivorMsg))
    }
}

const val survivorCheckJson = """{"success": true}"""

const val survivorNewJson = """
{
  "id": "",
  "title": "",
  "firstName": "Jesse",
  "lastName": "Pinkman",
  "gender": "male",
  "classId": "unassigned",
  "voice": "white-m"
}
"""
/*
{
  "id": null,
  "title": null,
  "firstName": null,
  "lastName": null,
  "gender": null,
  "portrait": null,
  "classId": null,
  "morale": null,
  "injuries": null,
  "missionId": null,
  "assignmentId": null,
  "reassignTimer": null,
  "appearance": null,
  "scale": null,
  "voice": null
}
*/