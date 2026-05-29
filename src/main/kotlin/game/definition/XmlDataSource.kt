package game.definition

import encore.definition.GameDataSource
import java.io.File
import java.util.zip.GZIPInputStream

class XmlDataSource(override val path: String): GameDataSource {
    private val file = File(path)

    init {
        if (!file.exists()) {
            throw IllegalArgumentException("File doesn't exist: $path")
        }
    }

    override fun readText(): String {
        return "You should call readBytes :)"
    }

    override fun readBytes(): ByteArray {
        GZIPInputStream(file.inputStream()).use {
            return it.readAllBytes()
        }
    }
}
