package game.domain.items

import encore.subunit.Subunit
import encore.subunit.scope.PlayerScope

class InventorySubunit(
    private val inventoryRepository: InventoryRepository
) : Subunit<PlayerScope> {
    override suspend fun debut(scope: PlayerScope): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun disband(scope: PlayerScope): Result<Unit> {
        return Result.success(Unit)
    }
}
