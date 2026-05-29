package game.domain.metadata

import encore.datastore.collection.PlayerId
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.PlayerScope
import game.domain.metadata.model.PlayerFlags

class PlayerObjectsMetadataSubunit(
    private val playerObjectsMetadataRepository: PlayerObjectsMetadataRepository
) : Subunit<PlayerScope> {
    private var flags: ByteArray = PlayerFlags.newgame() // use newgame flags to avoid null
    private var nickname: String? = null // nickname null will prompt leader creation
    private lateinit var playerId: PlayerId

    suspend fun updatePlayerFlags(flags: ByteArray) {
        val result = playerObjectsMetadataRepository.updatePlayerFlags(playerId, flags)
        result.onFailure {
            Fancam.error(tag = "metadata") { "Error updatePlayerFlags" }
        }
        result.onSuccess {
            this.flags = flags
        }
    }

    suspend fun updatePlayerNickname(nickname: String) {
        val result = playerObjectsMetadataRepository.updatePlayerNickname(playerId, nickname)
        result.onFailure {
            Fancam.error(tag = "metadata") { "Error updatePlayerNickname" }
        }
        result.onSuccess {
            this.nickname = nickname
        }
    }

    fun getPlayerFlags() = flags
    fun getPlayerNickname() = nickname

    override suspend fun debut(scope: PlayerScope): Result<Unit> {
        return runCatching {
            this.playerId = scope.playerId
            val _flags = playerObjectsMetadataRepository.getPlayerFlags(playerId).getOrThrow()
            val _nickname = playerObjectsMetadataRepository.getPlayerNickname(playerId).getOrThrow()

            flags = _flags
            nickname = _nickname

            if (flags.isEmpty()) {
                Fancam.warn(tag = "metadata") { "flags for playerId=$playerId is empty" }
            }
        }
    }

    override suspend fun disband(scope: PlayerScope): Result<Unit> {
        return Result.success(Unit)
    }
}
