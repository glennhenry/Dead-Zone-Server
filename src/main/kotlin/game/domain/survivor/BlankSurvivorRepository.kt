package game.domain.survivor

import encore.datastore.collection.PlayerId
import game.domain.survivor.model.Survivor

class BlankSurvivorRepository: SurvivorRepository {
    override suspend fun getSurvivors(playerId: PlayerId): Result<List<Survivor>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateSurvivor(
        playerId: PlayerId,
        srvId: String,
        updatedSurvivor: Survivor
    ): Result<Unit> {
        TODO("Not yet implemented")
    }
}
