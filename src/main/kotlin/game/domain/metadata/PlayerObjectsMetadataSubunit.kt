package game.domain.metadata

import game.mongo.collection.PlayerId
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.PlayerScope
import encore.utils.types.Report
import encore.utils.types.toReport
import game.domain.metadata.model.PlayerFlags

class PlayerObjectsMetadataSubunit(
    private val playerObjectsMetadataRepository: PlayerObjectsMetadataRepository
) : Subunit<PlayerScope> {
    private var flags: ByteArray = PlayerFlags.newgame() // use newgame flags to avoid null
    private var nickname: String? = null // nickname null will prompt leader creation
    private lateinit var playerId: PlayerId

    suspend fun updatePlayerFlags(flags: ByteArray): Report {
        return playerObjectsMetadataRepository.updatePlayerFlags(playerId, flags)
            .onFailure {
                Fancam.error(it, tag = "metadata") { "Error updatePlayerFlags" }
            }
            .onSuccess {
                this.flags = flags
            }
            .toReport()
    }

    suspend fun updatePlayerNickname(nickname: String): Report {
        return playerObjectsMetadataRepository.updatePlayerNickname(playerId, nickname)
            .onFailure {
                Fancam.error(it, tag = "metadata") { "Error updatePlayerNickname" }
            }
            .onSuccess {
                this.nickname = nickname
            }
            .toReport()
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
