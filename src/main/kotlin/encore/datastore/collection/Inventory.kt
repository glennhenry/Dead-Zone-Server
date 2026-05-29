package encore.datastore.collection

import game.Globals
import kotlinx.serialization.Serializable

/**
 * Inventory table
 */
@Serializable
data class Inventory(
    val playerId: PlayerId, // reference to UserDocument
    val inventory: List<game.domain.items.model.Item> = emptyList(),
    val schematics: ByteArray = byteArrayOf(),  // see line 643 of Inventory.as
) {
    companion object {
        fun admin(): Inventory {
            val items = listOf(
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "crate-tutorial"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "key-herc-level-1").copy(new = false, qty = 10u),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "grenade-christmas-2"),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "p90").copy(level = 37, quality = 3),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "sword-unique").copy(level = 49, quality = 51),
                game.domain.items.ItemFactory.createItemFromId(itemId = Globals.FIGHTER_WEP_ID, "bladesaw")
                    .copy(level = 58, quality = 50),
                game.domain.items.ItemFactory.createItemFromId(itemId = Globals.PLAYER_WEP_ID, "freedom-desert-eagle-2-replica")
                    .copy(level = 49, quality = 100),
                game.domain.items.ItemFactory.createItemFromId(itemId = Globals.RECON_WEP_ID, "fal-winter-2017-3")
                    .copy(level = 59, quality = 100),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "goldAK47-special").copy(level = 19, quality = 100, bind = 1u),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "helmet-wasteland-knight").copy(level = 50, quality = 100),
                game.domain.items.ItemFactory.createItemFromId(idInXML = "christmas-canned-meat")
            )

            return Inventory(
                playerId = Globals.ADMIN_PLAYER_ID,
                inventory = items,
                schematics = byteArrayOf()
            )
        }

        fun newgame(pid: String): Inventory {
            val items = listOf(
                game.domain.items.model.Item(type = "pocketKnife"),
                game.domain.items.model.Item(type = "lawson22")
            )
            return Inventory(
                playerId = pid,
                inventory = items,
                schematics = byteArrayOf()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Inventory

        if (inventory != other.inventory) return false
        if (!schematics.contentEquals(other.schematics)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inventory.hashCode()
        result = 31 * result + schematics.contentHashCode()
        return result
    }
}
