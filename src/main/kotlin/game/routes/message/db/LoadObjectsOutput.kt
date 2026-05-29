package game.routes.message.db

import game.routes.rpc.BigDBConverter
import kotlinx.serialization.Serializable

@Serializable
data class LoadObjectsOutput(
    val objects: List<BigDBObject> = listOf()
) {
    companion object {
        inline fun <reified T : Any> fromData(obj: T): BigDBObject {
            return BigDBConverter.toBigDBObject(obj = obj)
        }
    }
}
