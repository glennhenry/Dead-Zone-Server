package game.domain.items.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ItemQualityType(val value: Int) {
    companion object {
        fun fromString(s: String): Int? {
            return when (s) {
                "none" -> -2147483648
                "grey" -> -1
                "white" -> 0
                "green" -> 1
                "blue" -> 2
                "purple" -> 3
                "rare" -> 50
                "unique" -> 51
                "infamous" -> 52
                "premium" -> 100
                else -> null
            }
        }

        fun fromInt(i: Int): String? {
            return when (i) {
                -2147483648 -> "none"
                -1 -> "grey"
                0 -> "white"
                1 -> "green"
                2 -> "blue"
                3 -> "purple"
                50 -> "rare"
                51 -> "unique"
                52 -> "infamous"
                100 -> "premium"
                else -> null
            }
        }
    }
}

object ItemQualityType_Constants {
    val NONE = game.domain.items.model.ItemQualityType(-2147483648)
    val GREY = game.domain.items.model.ItemQualityType(-1)
    val WHITE = game.domain.items.model.ItemQualityType(0)
    val GREEN = game.domain.items.model.ItemQualityType(1)
    val BLUE = game.domain.items.model.ItemQualityType(2)
    val PURPLE = game.domain.items.model.ItemQualityType(3)
    val RARE = game.domain.items.model.ItemQualityType(50)
    val UNIQUE = game.domain.items.model.ItemQualityType(51)
    val INFAMOUS = game.domain.items.model.ItemQualityType(52)
    val PREMIUM = game.domain.items.model.ItemQualityType(100)
}
