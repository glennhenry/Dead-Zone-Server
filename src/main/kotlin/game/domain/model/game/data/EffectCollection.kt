package game.domain.model.game.data

import kotlinx.serialization.Serializable

@Serializable
data class EffectCollection(
    val list: List<ByteArray> = listOf()
) {
    fun dummy(): EffectCollection {
        return EffectCollection(
            list = listOf(byteArrayOf())
        )
    }
}
