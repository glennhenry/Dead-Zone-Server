package game.domain.survivor

import encore.datastore.collection.PlayerId
import game.domain.survivor.model.Survivor

interface SurvivorRepository {
    /**
     * Get survivors of [playerId].
     */
    suspend fun getSurvivors(playerId: PlayerId): Result<List<Survivor>>

    /**
     * Update survivor field of [playerId] for the [srvId]
     */
    suspend fun updateSurvivor(
        playerId: PlayerId,
        srvId: String,
        updatedSurvivor: Survivor
    ): Result<Unit>
}
