package game.domain.compound

import encore.datastore.collection.PlayerId
import game.domain.compound.model.BuildingLike
import game.domain.compound.model.GameResources

class BlankCompoundRepository: CompoundRepository {
    override suspend fun getGameResources(playerId: PlayerId): Result<GameResources> {
        TODO("Not yet implemented")
    }

    override suspend fun updateGameResources(
        playerId: PlayerId,
        newResources: GameResources
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun createBuilding(
        playerId: PlayerId,
        newBuilding: BuildingLike
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getBuildings(playerId: PlayerId): Result<List<BuildingLike>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateBuilding(
        playerId: PlayerId,
        bldId: String,
        updatedBuilding: BuildingLike
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBuilding(
        playerId: PlayerId,
        bldId: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }
}
