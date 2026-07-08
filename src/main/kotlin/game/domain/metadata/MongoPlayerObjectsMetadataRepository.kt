package game.domain.metadata

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import encore.datastore.collection.PlayerId
import encore.datastore.collection.PlayerObjects
import encore.datastore.runMongoCatching
import encore.datastore.throwIfNotModified
import kotlinx.coroutines.flow.firstOrNull

class MongoPlayerObjectsMetadataRepository(
    private val objCollection: MongoCollection<PlayerObjects>
) : PlayerObjectsMetadataRepository {

    override suspend fun getPlayerFlags(playerId: PlayerId): Result<ByteArray> {
        return runMongoCatching("No player found with id=$playerId") {
            objCollection
                .find(Filters.eq("playerId", playerId))
                .firstOrNull()
                ?.flags
        }
    }

    override suspend fun updatePlayerFlags(playerId: PlayerId, flags: ByteArray): Result<Unit> {
        return runMongoCatching("No player found with id=$playerId") {
            val filter = Filters.eq("playerId", playerId)
            val update = Updates.set("flags", flags)
            objCollection
                .updateOne(filter, update)
                .throwIfNotModified("updatePlayerFlags", { filter }, { update })
        }
    }

    override suspend fun getPlayerNickname(playerId: PlayerId): Result<String?> {
        return runMongoCatching("No player found with id=$playerId") {
            objCollection
                .find(Filters.eq("playerId", playerId))
                .firstOrNull()
                ?.nickname
        }
    }

    override suspend fun updatePlayerNickname(playerId: PlayerId, nickname: String): Result<Unit> {
        return runMongoCatching("No player found with id=$playerId") {
            val filter = Filters.eq("playerId", playerId)
            val update = Updates.set("nickname", nickname)
            objCollection
                .updateOne(filter, update)
                .throwIfNotModified("updatePlayerNickname", { filter }, { update })
        }
    }
}
