package game.domain.items

import encore.datastore.collection.Inventory
import game.domain.items.model.Item

interface InventoryRepository {
    suspend fun getInventory(playerId: String): Result<Inventory>
    suspend fun updateInventory(playerId: String, updatedInventory: List<Item>): Result<Unit>
    suspend fun updateSchematics(playerId: String, updatedSchematics: ByteArray): Result<Unit>
}
