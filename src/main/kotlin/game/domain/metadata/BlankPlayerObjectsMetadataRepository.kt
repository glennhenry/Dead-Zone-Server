package game.domain.metadata

import encore.datastore.collection.PlayerId

class BlankPlayerObjectsMetadataRepository: PlayerObjectsMetadataRepository {
    override suspend fun getPlayerFlags(playerId: PlayerId): Result<ByteArray> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlayerFlags(
        playerId: PlayerId,
        flags: ByteArray
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getPlayerNickname(playerId: PlayerId): Result<String?> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlayerNickname(
        playerId: PlayerId,
        nickname: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }
}
