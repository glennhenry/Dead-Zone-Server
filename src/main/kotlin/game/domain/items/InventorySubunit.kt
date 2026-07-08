package game.domain.items

import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.PlayerScope
import encore.utils.types.Report
import encore.utils.types.toReport
import game.domain.items.model.Item

class InventorySubunit(
    private val inventoryRepository: InventoryRepository
) : Subunit<PlayerScope> {
    private var inventory = listOf<Item>()
    private var schematics = byteArrayOf()
    private lateinit var playerId: String

    fun getInventory(): List<Item> = inventory
    fun getSchematics(): ByteArray = schematics

    suspend fun updateInventory(
        updateAction: suspend (List<Item>) -> List<Item>
    ): Report {
        val updatedInventory = updateAction(this.inventory)
        return inventoryRepository.updateInventory(playerId, updatedInventory)
            .onFailure {
                Fancam.error(tag = "inventory") {
                    "Failed to update inventory (from size=${this.inventory.size} to size=${updatedInventory.size})"
                }
            }
            .onSuccess {
                inventory = updatedInventory
            }
            .toReport()
    }

    suspend fun updateSchematics(
        updateAction: suspend (ByteArray) -> ByteArray
    ): Report {
        val updatedSchematics = updateAction(schematics)
        return inventoryRepository.updateSchematics(playerId, updatedSchematics)
            .onFailure {
                Fancam.error(tag = "inventory") {
                    "Failed to update schematics (from size=${this.schematics.size} to size=${updatedSchematics.size})"
                }
            }
            .onSuccess {
                schematics = updatedSchematics
            }
            .toReport()
    }

    override suspend fun debut(scope: PlayerScope): Result<Unit> {
        return runCatching {
            this.playerId = scope.playerId
            val inventoryObject = inventoryRepository.getInventory(playerId).getOrThrow()
            inventory = inventoryObject.inventory
            schematics = inventoryObject.schematics
        }
    }

    override suspend fun disband(scope: PlayerScope): Result<Unit> {
        return Result.success(Unit)
    }
}
