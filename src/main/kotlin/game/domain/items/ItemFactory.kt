package game.domain.items

import encore.definition.GameReference
import encore.utils.identifier.Ids
import game.definition.ItemResource
import game.definition.ItemsDefinition
import game.domain.items.model.Item
import game.domain.items.model.ItemQualityType
import org.w3c.dom.Element

object ItemFactory {
    private val itemsDefinition = GameReference.get<ItemsDefinition>()

    fun getRandomItem(): Item {
        return createItemFromResource(res = itemsDefinition.itemsById.values.random())
    }

    fun createItemFromId(itemId: String = Ids.uuid(), idInXML: String): Item {
        val res =
            itemsDefinition.itemsById[idInXML]
                ?: throw IllegalArgumentException("Failed creating Item id=$itemId from xml id=$idInXML (xml id not found)")
        return createItemFromResource(itemId, res)
    }

    fun createItemFromResource(itemId: String = Ids.uuid(), res: ItemResource): Item {
        val baseItem = Item(
            id = itemId,
            type = res.idInXML,
            quality = ItemQualityType.fromString(
                res.element.getAttribute(
                    "quality"
                )
            )
        )

        when (res.type) {
            "gear" -> parseGear(res.element, baseItem)
            "weapon" -> parseWeapon(res.element, baseItem)
            "clothing" -> parseClothing(res.element, baseItem)
            // and many more...
        }

        return baseItem
    }

    // these should modify the base item as needed
    // currently does nothing as we are not sure the detailed traits of each item types
    private fun parseGear(element: Element, baseItem: Item) {}
    private fun parseWeapon(element: Element, baseItem: Item) {}
    private fun parseClothing(element: Element, baseItem: Item) {}
}
