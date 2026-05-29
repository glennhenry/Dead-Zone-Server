package game.domain.compound

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.collection.PlayerId
import encore.datastore.collection.PlayerObjects
import encore.datastore.runMongoCatching
import encore.datastore.throwIfNotModified
import game.domain.compound.model.BuildingLike
import game.domain.compound.model.GameResources
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document

class MongoCompoundRepository(val objCollection: MongoCollection<PlayerObjects>) : CompoundRepository {
    override suspend fun getGameResources(playerId: PlayerId): Result<GameResources> {
        return runMongoCatching("No player found with id=$playerId") {
            val filter = Filters.eq("playerId", playerId)
            objCollection
                .find(filter)
                .firstOrNull()
                ?.resources
        }
    }

    override suspend fun updateGameResources(
        playerId: PlayerId, newResources: GameResources
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq("playerId", playerId)
            val updateSet = Updates.set("resources", newResources)

            objCollection.updateOne(filter, updateSet)
                .throwIfNotModified("updateGameResources")
        }
    }

    override suspend fun createBuilding(playerId: PlayerId, newBuilding: BuildingLike): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq("playerId", playerId)
            val updateAdd = Updates.addToSet("buildings", newBuilding)

            objCollection.updateOne(filter, updateAdd)
                .throwIfNotModified("createBuilding")
        }
    }

    override suspend fun getBuildings(playerId: PlayerId): Result<List<BuildingLike>> {
        return runMongoCatching("No player found with id=$playerId") {
            val filter = Filters.eq("playerId", playerId)
            objCollection
                .find(filter)
                .firstOrNull()
                ?.buildings
        }
    }

    override suspend fun updateBuilding(
        playerId: PlayerId,
        bldId: String,
        updatedBuilding: BuildingLike
    ): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.and(
                Filters.eq("playerId", playerId),
                Filters.eq("buildings.id", bldId)
            )
            val update = Updates.set("buildings.$", updatedBuilding)

            objCollection.updateOne(filter, update)
                .throwIfNotModified("updateBuilding")
        }
    }

    override suspend fun deleteBuilding(playerId: PlayerId, bldId: String): Result<Unit> {
        return runMongoCatching {
            val filter = Filters.eq("playerId", playerId)
            val updateDelete = Updates.pull("buildings", Document("id", bldId))

            objCollection.updateOne(filter, updateDelete)
                .throwIfNotModified("deleteBuilding")
        }
    }
}
