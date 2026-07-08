package game.domain.survivor

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.FieldPlayerId
import encore.datastore.collection.PlayerId
import encore.datastore.collection.PlayerObjects
import encore.datastore.runMongoCatching
import encore.datastore.throwIfNotModified
import game.domain.survivor.model.Survivor
import kotlinx.coroutines.flow.firstOrNull

val FieldSurvivors = PlayerObjects::survivors.name

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
            objCollection.updateOne(filter, update)
                .throwIfNotModified("updateSurvivor", { filter }, { update })
        }
    }

    override suspend fun addSurvivor(
        playerId: String,
        survivor: Survivor
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq(FieldPlayerId, playerId)
            val updateAdd = Updates.addToSet(FieldSurvivors, survivor)
            objCollection.updateOne(filter, updateAdd)
                .throwIfNotModified("addSurvivor", { filter }, { updateAdd })
        }
    }
}
