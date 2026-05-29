package game.domain.survivor

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.collection.PlayerId
import encore.datastore.collection.PlayerObjects
import encore.datastore.runMongoCatching
import game.domain.survivor.model.Survivor
import kotlinx.coroutines.flow.firstOrNull

class MongoSurvivorRepository(val objCollection: MongoCollection<PlayerObjects>) :
    SurvivorRepository {
    /**
     * Get survivors of [playerId], returning an empty list if nothing is present.
     */
    override suspend fun getSurvivors(playerId: PlayerId): Result<List<Survivor>> {
        return runMongoCatching("No player found with id=$playerId") {
            objCollection
                .find(Filters.eq("playerId", playerId))
                .firstOrNull()
                ?.survivors
        }
    }

    override suspend fun updateSurvivor(
        playerId: PlayerId,
        srvId: String,
        updatedSurvivor: Survivor
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.and(
                Filters.eq("playerId", playerId),
                Filters.eq("survivors.id", srvId)
            )
            val update = Updates.set("survivors.$", updatedSurvivor)
            val result = objCollection.updateOne(filter, update)

            if (result.matchedCount != 1L) {
                throw NoSuchElementException("No player found with id=$playerId")
            }

            if (result.modifiedCount != 1L) {
                throw NoSuchElementException("Survivor for playerId=$playerId srvId=$srvId not found")
            }

            Unit
        }
    }
}
