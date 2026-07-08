package game.definition

import encore.definition.GameDataLoader
import encore.definition.GameDataSource
import encore.definition.GameDefinition
import game.domain.compound.model.GameResources
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * You will need to read the XML resources in data/xml/. This is to sync with client-side data.
 * Sometimes the client-side does not send every data (like what zombie it request or what loot it expects)
 * This is because it expects the server to handle it, including data lookup to those XMLs.
 *
 * - For example, items.xml read includes initializing in-memory index such as itemsById, itemsByType, allowing easy retrieval.
 * - zombie.xml may includes zombies of same type with different level (use this to choose which zombie to spawn depend on area level).
 * - zombie.xml also have rarity, use this to determine the rarity of spawning that zombie (Yes we need zombie system just like we have the loot system).
 * - For buildings.xml maybe you will need the data for some buildings like production (reading the prod element) to see its production rate, etc.
 * - badwords.xml contains useful badword utility. We may use it as verification (not very important here).
 * - I haven't checked other.
 *
 * VERY IMPORTANT: there is confusion around id, type for all XML resources.
 * - in the server, id is treated as unique identifier,
 * type as the 'id' in the XML,
 * and server does not store extra "type" which correspond to the 'type' in XML.
 * - in the game, lookup to XML is done in syntax like @id == type. This means that the id (at) XML must be equal to type sent from server.
 * - For type in XML lookup, you can make similar server index like itemsByType
 */
class ItemResource(
    val idInXML: String,
    val type: String,
    val element: Element
) {
    override fun toString(): String {
        return "ItemResource(idInXML=$idInXML, type=$type)"
    }
}

class ItemsDefinition : GameDefinition {
    val itemsById = mutableMapOf<String, ItemResource>()
    val itemsByIdUppercased = mutableMapOf<String, ItemResource>()
    val itemsByType = mutableMapOf<String, MutableList<ItemResource>>()
    val itemsByLootable = mutableMapOf<String, MutableList<ItemResource>>()

    /**
     * Find an item resource by ID, preferring exact match first.
     *
     * It is preferred to use this method to find an item resource.
     * The game is known to be inconsistent for referencing item ID.
     * Sometimes, it sends uppercased ID although items.xml definition is case-sensitive.
     */
    fun findItem(id: String): ItemResource? {
        return itemsById[id] ?: itemsByIdUppercased[id.uppercase()]
    }

    fun requireItem(idInXml: String): ItemResource {
        return requireNotNull(findItem(idInXml)) { "Items with ID in XML $idInXml is missing from index" }
    }

    fun isResourceItem(idInXml: String): Boolean {
        return requireItem(idInXml).type == "resource"
    }

    /**
     * Get max stack of certain item ID
     *
     * @return `Int` the number of maximum stack,
     * 1 as default value if stack amount is not listed in the XML.
     */
    fun getMaxStackOfItem(idInXml: String): Int {
        val item = requireItem(idInXml)
        val stackNode = item.element.getElementsByTagName("stack")
        return (stackNode.item(0) as? Element)?.textContent?.trim()?.toIntOrNull() ?: 1
    }

    /**
     * Get the resource amount of a certain item ID.
     *
     * @return [GameResources] object with resource amount set according to the item.
     * Return `null` if the ID is not a resource type of item.
     */
    fun getResourceAmount(idInXml: String): GameResources? {
        val item = requireItem(idInXml)
        if (item.type != "resource") return null

        val resourceMap = mutableMapOf<String, Int>()

        val resNodes = item.element.getElementsByTagName("res")
        for (i in 0 until resNodes.length) {
            val resItemsNodes = resNodes.item(i) as? Element ?: continue
            val itemId = resItemsNodes.getAttribute("id")
            val amount = resItemsNodes.textContent.trim().toIntOrNull() ?: 0
            resourceMap[itemId] = resourceMap.getOrDefault(itemId, 0) + amount
        }

        return GameResources(
            wood = resourceMap["wood"] ?: 0,
            metal = resourceMap["metal"] ?: 0,
            cloth = resourceMap["cloth"] ?: 0,
            water = resourceMap["water"] ?: 0,
            food = resourceMap["food"] ?: 0,
            ammunition = resourceMap["ammunition"] ?: 0,
            cash = resourceMap["cash"] ?: 0
        )
    }
}

class ItemsXmlDataLoader : GameDataLoader {
    override fun produce(source: GameDataSource): List<GameDefinition> {
        val def = ItemsDefinition()

        // from xml
        val stream = ByteArrayInputStream(source.readBytes())
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)

        val items = document.getElementsByTagName("item")
        for (i in 0 until items.length) {
            val itemNode = items.item(i) as? Element ?: continue
            val itemId = itemNode.getAttribute("id").takeIf { it != "" } ?: continue
            val itemType = itemNode.getAttribute("type").takeIf { it != "" } ?: continue
            val itemLocs = itemNode.getAttribute("locs").takeIf { it != "" }

            val res = ItemResource(itemId, itemType, itemNode)

            def.itemsById.putIfAbsent(itemId, res)
            def.itemsByIdUppercased.putIfAbsent(itemId.uppercase(), res)
            def.itemsByType.computeIfAbsent(itemId) { mutableListOf() }.add(res)
            if (itemLocs?.isNotEmpty() ?: false) {
                val locList = itemLocs.split(',').map { it.trim() }
                for (loc in locList) {
                    def.itemsByLootable.computeIfAbsent(loc) { mutableListOf() }.add(res)
                }
            }
        }

        return listOf(def)
    }
}
