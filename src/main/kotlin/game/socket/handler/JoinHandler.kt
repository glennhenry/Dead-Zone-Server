package game.socket.handler

import encore.context.ServerContext
import encore.fancam.Fancam
import encore.network.fanchant.FanchantType
import encore.network.handler.FanchantHandler
import encore.network.handler.HandlerContext
import encore.time.TimeCenter
import encore.utils.types.okOrNull
import game.domain.login.LoginStateBuilder
import game.socket.protocol.NetworkMessage
import game.socket.protocol.PIOSerializer
import game.socket.protocol.PioFanchant
import game.socket.protocol.PioFanchantType
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPOutputStream

/**
 * Handle `join` message by:
 *
 * 1. Sending `playerio.joinresult`
 * 2. Sending `gr` message
 */
class JoinHandler(private val serverContext: ServerContext) : FanchantHandler<PioFanchant> {
    override val fanchantType: FanchantType<PioFanchant> = PioFanchantType(NetworkMessage.JOIN)

    override suspend fun handle(ctx: HandlerContext<PioFanchant>): Unit = with(ctx) {
        val joinKey = fanchant.getString(NetworkMessage.JOIN)
        Fancam.debug { "Handling join with key: $joinKey" }

        val userId = fanchant.getString("serviceUserId")
            ?: throw IllegalArgumentException("No userId for connection: $connection")

        val username = fanchant.getString("nickname")
        if (username == null) {
            Fancam.warn { "nickname key is null" }
        }

        val displayName = serverContext.subunits.account.getProfile(userId).okOrNull()?.displayName ?: "noname"
        connection.acknowledge(userId, displayName)

        // First message: join result
        val joinResultMsg = listOf(NetworkMessage.JOIN_RESULT, true)
        connection.write(PIOSerializer.serialize(joinResultMsg))

        // Create PlayerContext which initializes per-player services
        serverContext.contextRegistry.createContext(
            playerId = connection.playerId,
            connection = connection,
        )

        // Second message: game ready message
        val gameReadyMsg = listOf(
            NetworkMessage.GAME_READY,
            TimeCenter.game.nowInDouble(),
            produceBinaries(),
            loadRawFile("assets/data/cost_table.json"),
            loadRawFile("assets/data/srv_table.json"),
            LoginStateBuilder.build(serverContext, connection.playerId)
        )
        connection.write(PIOSerializer.serialize(gameReadyMsg))
    }

    /**
     * Pack all xml.gz resources in data/xml/ and manually added compressed
     * resources_secondary.xml.gz in data/
     *
     * Core.swf doesn't request these, the server has to send it manually.
     */
    fun produceBinaries(): ByteArray {
        val xmlResources = listOf(
            "assets/game/data/resources_secondary.xml",
            "assets/game/data/resources_mission.xml",
            "assets/game/data/xml/alliances.xml.gz",
            "assets/game/data/xml/arenas.xml.gz",
            "assets/game/data/xml/attire.xml.gz",
            "assets/game/data/xml/badwords.xml.gz",
            "assets/game/data/xml/buildings.xml.gz",
            "assets/game/data/xml/config.xml.gz",
            "assets/game/data/xml/crafting.xml.gz",
            "assets/game/data/xml/effects.xml.gz",
            "assets/game/data/xml/humanenemies.xml.gz",
            "assets/game/data/xml/injury.xml.gz",
            "assets/game/data/xml/itemmods.xml.gz",
            "assets/game/data/xml/items.xml.gz",
            "assets/game/data/xml/quests.xml.gz",
            "assets/game/data/xml/quests_global.xml.gz",
            "assets/game/data/xml/raids.xml.gz",
            "assets/game/data/xml/skills.xml.gz",
            "assets/game/data/xml/streetstructs.xml.gz",
            "assets/game/data/xml/survivor.xml.gz",
            "assets/game/data/xml/vehiclenames.xml.gz",
            "assets/game/data/xml/zombie.xml.gz",
            "assets/game/data/xml/scenes/compound.xml.gz",
            "assets/game/data/xml/scenes/interior-gunstore-1.xml.gz",
            "assets/game/data/xml/scenes/street-small-1.xml.gz",
            "assets/game/data/xml/scenes/street-small-2.xml.gz",
            "assets/game/data/xml/scenes/street-small-3.xml.gz",
            "assets/game/data/xml/scenes/set-motel.xml.gz",
        )

        val output = ByteArrayOutputStream()

        // 1. Write number of files as a single byte
        output.write(xmlResources.size)

        for (path in xmlResources) {
            File(path).inputStream().use {
                val rawBytes = it.readBytes()

                val fileBytes = if (path.endsWith(".gz")) {
                    rawBytes
                } else {
                    val compressed = ByteArrayOutputStream()
                    GZIPOutputStream(compressed).use { gzip ->
                        gzip.write(rawBytes)
                    }
                    compressed.toByteArray()
                }

                val uri = path
                    .removePrefix("assets/game/data/")
                    .removeSuffix(".gz")
                val uriBytes = uri.toByteArray(Charsets.UTF_8)

                // 2. Write URI length as 2-byte little endian
                output.writeShortLE(uriBytes.size)

                // 3. Write URI bytes
                output.write(uriBytes)

                // 4. Write file size as 4-byte little endian
                output.writeIntLE(fileBytes.size)

                // 5. Write file data
                output.write(fileBytes)
            }
        }

        return output.toByteArray()
    }

    fun loadRawFile(path: String): String {
        return File(path).readText()
    }
}

fun ByteArrayOutputStream.writeShortLE(value: Int) {
    val buf = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value.toShort())
    write(buf.array())
}

fun ByteArrayOutputStream.writeIntLE(value: Int) {
    val buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value)
    write(buf.array())
}
