package gameTest

import encore.definition.GameReference
import game.definition.ItemsDefinition
import game.definition.ItemsXmlDataLoader
import game.definition.XmlDataSource
import game.domain.compound.model.GameResources
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestItemsDefinition {
    private val def = run {
        GameReference.initialize {
            add(XmlDataSource("assets/game/data/xml/items.xml.gz"), ItemsXmlDataLoader())
        }
        GameReference.get<ItemsDefinition>()
    }

    @Test
    fun `isResourceItem non-resource should return false`() {
        assertTrue(def.isResourceItem("cannedCorn"))
    }

    @Test
    fun `isResourceItem actual resource should return true`() {
        assertFalse(def.isResourceItem("key-halloween-2016-budget"))
    }

    @Test
    fun `getResourceAmount fuel-cans returns 10 cash`() {
        expectResource("fuel-cans", GameResources(cash = 10))
    }

    @Test
    fun `getResourceAmount cannedCorn returns 2 food`() {
        expectResource("cannedCorn", GameResources(food = 2))
    }

    @Test
    fun `getResourceAmount metalBeam returns 30 metal`() {
        expectResource("metalBeam", GameResources(metal = 30))
    }

    @Test
    fun `getResourceAmount ammo3 returns 50 ammo`() {
        expectResource("ammo3", GameResources(ammunition = 50))
    }

    private fun expectResource(idInXml: String, expected: GameResources) {
        def.getResourceAmount(idInXml)?.let { assertEquals(expected, it) }
    }

    @Test
    fun `getMaxStackOf rope stack is listed returns 10`() {
        expectStack("rope", 10)
    }

    @Test
    fun `getMaxStackOf water stack is listed returns 500`() {
        expectStack("water", 500)
    }

    @Test
    fun `getMaxStackOf rifle stack not listed returns 1`() {
        expectStack("rifle", 1)
    }

    private fun expectStack(idInXml: String, expected: Int) {
        assertEquals(expected, def.getMaxStackOfItem(idInXml))
    }
}