package game.domain.metadata

import encore.datastore.collection.PlayerId

/**
 * Repository for uncategorized fields in PlayerObjects like nickname, flags
 */
interface PlayerObjectsMetadataRepository {
    suspend fun getPlayerFlags(playerId: PlayerId): Result<ByteArray>
    suspend fun updatePlayerFlags(playerId: PlayerId, flags: ByteArray): Result<Unit>

    suspend fun getPlayerNickname(playerId: PlayerId): Result<String?>
    suspend fun updatePlayerNickname(playerId: PlayerId, nickname: String): Result<Unit>
}
