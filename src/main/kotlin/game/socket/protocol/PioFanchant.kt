package game.socket.protocol

import encore.network.fanchant.Fanchant
import encore.network.fanchant.FanchantType
import encore.serialization.JSON
import encore.serialization.parseJsonToMap

/**
 * Representation of game message sent to the socket server.
 *
 * Message is always a flat list of even length (if odd then the first is the type).
 * Two of each element is paired as key-value pair.
 *
 * @property raw deserialized data received from socket connection
 */
class PioFanchant(
    messageType: String,
    private val raw: List<Any>
) : Fanchant {
    override val type: FanchantType<*> = PioFanchantType(messageType)

    private val data: Map<String, Any?> = buildMap {
        val start = if (type.id != "<no-type>") 1 else 0
        val end = raw.size
        for (i in start until end step 2) {
            val key = raw.getOrNull(i) as? String ?: continue
            val value = raw.getOrNull(i + 1)
            put(key, value)
        }
    }

    fun isEmpty(): Boolean {
        return data.keys.isEmpty()
    }

    /**
     * Get a value (`any` type) from particular key.
     * Use [getString], [getInt], etc for typed result
     *
     * @param key
     * @return the value from the corresponding key in the message
     */
    fun get(key: String): Any? = data[key]

    fun contains(key: String): Boolean {
        return data.containsKey(key)
    }

    fun getString(key: String): String? = data[key] as? String
    fun getInt(key: String): Int? = (data[key] as? Number)?.toInt()
    fun getBoolean(key: String): Boolean? = data[key] as? Boolean
    fun getBytes(key: String): ByteArray? = data[key] as? ByteArray
    fun getMap(key: String): Map<String, Any?>? {
        val rawValue = data[key] ?: return null
        return when (rawValue) {
            is Map<*, *> -> rawValue as? Map<String, Any?>
            is String -> {
                try {
                    parseJsonToMap(JSON.json, rawValue)
                } catch (_: Exception) {
                    null
                }
            }

            else -> null
        }
    }

    fun getList(key: String): List<Any?>? = data[key] as? List<*>

    fun keys(): Set<String> = data.keys
    fun values(): Collection<Any?> = data.values

    override fun toString(): String = "PioFanchant(type=$type, data=$data)"

    companion object {
        fun fromRaw(raw: List<Any>): PioFanchant {
            val type: String = if (raw.size % 2 == 1 && raw.size != 1) {
                raw.firstOrNull() as? String ?: "<no-type>"
            } else {
                "<no-type>"
            }

            return PioFanchant(type, raw)
        }
    }
}

data class PioFanchantType(override val id: String) : FanchantType<PioFanchant>
