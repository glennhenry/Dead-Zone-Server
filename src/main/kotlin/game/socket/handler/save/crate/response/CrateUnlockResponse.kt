package game.socket.handler.save.crate.response

import game.domain.items.ItemFactory
import game.domain.items.model.Item
import kotlinx.serialization.Serializable

@Serializable
data class CrateUnlockResponse(
    val success: Boolean = true,
    val item: game.domain.items.model.Item = game.domain.items.ItemFactory.getRandomItem(),
    val effect: String? = null,   // base64 encoded, parsed to [Effect]
    val cooldown: String? = null, // similar as above
    val keyId: String? = null, // to remove the key used to open the crate
    val keyQty: Int? = null, // to remove the key used to open the crate
    val crateId: String? = null, // to remove the opened crate
)

val gachaPoolExample = listOf(
    game.domain.items.ItemFactory.createItemFromId(idInXML = "exo-rig-heavyBrawler-replica"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "helmet-exo-brawler-replica"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "helmet-exo-targeting-replica"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "mask-herc-exo-replica"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "exo-undershirt-1-replica"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "exo-underpants-1-replica"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "pistol-halloween-reborn"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "pistol-halloween-2-reborn"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "rifle-halloween-reborn"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "rifle-halloween-2-reborn"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "sword-laser-purple-reborn"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "trident-halloween-reborn"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "trident-halloween-2-reborn"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "crossbow-halloween-2015-reborn"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "crossbow-halloween-2015-2-reborn"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "halloween-exo-zombie"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "bladesaw"),
    game.domain.items.ItemFactory.createItemFromId(idInXML = "warclub")
)
