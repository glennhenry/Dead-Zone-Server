package game.domain.compound

import encore.datastore.collection.PlayerId
import game.domain.compound.model.BuildingLike
import game.domain.compound.model.GameResources

interface CompoundRepository {
    // Resource C_U_
    suspend fun getGameResources(playerId: PlayerId): Result<GameResources>
    suspend fun updateGameResources(
        playerId: PlayerId,
        newResources: GameResources
    ): Result<Unit>

    // Building CRUD
    suspend fun createBuilding(
        playerId: PlayerId,
        newBuilding: BuildingLike
    ): Result<Unit>
    suspend fun getBuildings(playerId: PlayerId): Result<List<BuildingLike>>
    suspend fun updateBuilding(
        playerId: PlayerId,
        bldId: String,
        updatedBuilding: BuildingLike
    ): Result<Unit>
    suspend fun deleteBuilding(playerId: PlayerId, bldId: String): Result<Unit>
}
