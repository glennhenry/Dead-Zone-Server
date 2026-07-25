package game.domain.items

import game.mongo.collection.Inventory
import game.domain.items.model.Item

class BlankInventoryRepository: InventoryRepository {
    override suspend fun getInventory(playerId: String): Result<Inventory> {
        TODO("Not yet implemented")
    }

    override suspend fun updateInventory(
        playerId: String,
        updatedInventory: List<Item>
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateSchematics(
        playerId: String,
        updatedSchematics: ByteArray
    ): Result<Unit> {
        TODO("Not yet implemented")
    }
}
