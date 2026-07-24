package game.domain.items

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.FieldPlayerId
import encore.datastore.collection.Inventory
import encore.datastore.runMongoCatching
import encore.datastore.throwIfNothingMatched
import game.domain.items.model.Item
import kotlinx.coroutines.flow.firstOrNull

val FieldInventory = Inventory::inventory.name
val FieldSchematics = Inventory::schematics.name

class MongoInventoryRepository(
    private val inventoryCollection: MongoCollection<Inventory>
) : InventoryRepository {
    override suspend fun getInventory(playerId: String): Result<Inventory> {
        return runMongoCatching {
            inventoryCollection
                .find(Filters.eq(FieldPlayerId, playerId))
                .firstOrNull()
        }
    }

    override suspend fun updateInventory(
        playerId: String,
        updatedInventory: List<Item>
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldPlayerId, playerId)
            val update = Updates.set(FieldInventory, updatedInventory)
            inventoryCollection
                .updateOne(filter, update)
                .throwIfNothingMatched("updateInventory not updated for $playerId", { filter })
        }
    }

    override suspend fun updateSchematics(
        playerId: String,
        updatedSchematics: ByteArray
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldPlayerId, playerId)
            val update = Updates.set(FieldSchematics, updatedSchematics)
            inventoryCollection
                .updateOne(filter, update)
                .throwIfNothingMatched("updateSchematics not updated for $playerId", { filter })
        }
    }
}
