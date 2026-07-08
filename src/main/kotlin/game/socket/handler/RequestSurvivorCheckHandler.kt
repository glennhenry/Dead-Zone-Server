package game.socket.handler

import encore.context.ServerContext
import encore.context.requirePlayerContext
import encore.fancam.Fancam
import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import encore.time.TimeCenter
import encore.utils.types.onFail
import encore.utils.types.onOk
import game.domain.survivor.model.Survivor
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.PioFanchant
import kotlin.random.Random
import kotlin.reflect.KClass

/**
 * Handle `rsc` message by:
 *
 * 1. Sending a reponse in JSON with success set to true
 */
class RequestSurvivorCheckHandler(private val serverContext: ServerContext) : FanchantHandler<PioFanchant> {
    override val fanchantType: String = NetworkMessage.REQUEST_SURVIVOR_CHECK
    override val expectedFanchantClass: KClass<PioFanchant> = PioFanchant::class

    override suspend fun handle(ctx: HandlerContext<PioFanchant>): Unit = with(ctx) {
        val id = fanchant.getMap("rsc")?.get("id") as? String
        Fancam.debug { "Received RSC of saveId: $id" }

        val reponseMsg =
            listOf(
                NetworkMessage.SEND_RESPONSE,  // Message Type
                id ?: "m",                     // id
                TimeCenter.now().toDouble(),   // server time
                """{"success": true}"""        // response
            )


        val newSrv = generateSurvivor()
        val newSurvivorMsg = listOf(NetworkMessage.SURVIVOR_NEW, generateNewSurvivorJson(newSrv))

        val sbs = serverContext.requirePlayerContext(connection.playerId).subunits
        sbs.survivor.addNewSurvivor(newSrv)
            .onOk {
                connection.write(PIOSerializer.serialize(reponseMsg))
                connection.write(PIOSerializer.serialize(newSurvivorMsg))
            }
            .onFail {
                Fancam.warn { "Sent -1 to client because of addNewSurvivor failure" }
                connection.write(PIOSerializer.serialize(listOf(-1)))
            }
    }

    private fun generateSurvivor(): Survivor {
        val gender = if (Random.nextBoolean()) "male" else "female"
        val maleVoices = setOf("white-m", "black-m", "latino-m", "asian-m")
        val femaleVoices = setOf("white-f", "black-f", "latino-f")
        val name = (if (gender == "male") maleNames.random() else femaleNames.random()).split(" ")

        return Survivor(
            firstName = name[0],
            lastName = name[1],
            gender = gender,
            classId = "unassigned",
            voice = if (gender == "male") maleVoices.random() else femaleVoices.random(),
            title = "",
            morale = emptyMap(),
            injuries = emptyList(),
            level = 0,
            xp = 0,
            missionId = null,
            assignmentId = null,
            accessories = emptyMap(),
            maxClothingAccessories = 1,
        )
    }

    private fun generateNewSurvivorJson(srv: Survivor): String {
        return """
        {
            "id": "${srv.id}",
            "title": "${srv.title}",
            "firstName": "${srv.firstName}",
            "lastName": "${srv.lastName}",
            "gender": "${srv.gender}",
            "classId": "${srv.classId}",
            "voice": "${srv.voice}"
        }
        """.trimIndent()
    }

    val maleNames = setOf(
        "Kim Dayeon", "Ezaki Hikaru", "Kim Chaehyun",
        "Huening Bahiyyih", "Choi Yujin", "Kang Yeseo",
        "Shen Xiaoting", "Sakamoto Mashiro", "Seo Youngeun"
    )

    val femaleNames = setOf(
        "Kim Dayeon", "Ezaki Hikaru", "Kim Chaehyun",
        "Huening Bahiyyih", "Choi Yujin", "Kang Yeseo",
        "Shen Xiaoting", "Sakamoto Mashiro", "Seo Youngeun"
    )
}
