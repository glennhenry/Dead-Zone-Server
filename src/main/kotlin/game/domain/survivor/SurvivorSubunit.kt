package game.domain.survivor

import encore.datastore.collection.PlayerId
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.PlayerScope
import encore.utils.types.Report
import encore.utils.types.toReport
import game.domain.survivor.model.Survivor

/**
 * Manages survivors.
 */
class SurvivorSubunit(
    val survivorLeaderId: String,
    private val survivorRepository: SurvivorRepository
) : Subunit<PlayerScope> {
    private val survivors = mutableListOf<Survivor>()
    private lateinit var playerId: PlayerId // for simple debug

    fun getSurvivorLeader(): Survivor {
        return survivors.find { it.id == survivorLeaderId }
            ?: throw NoSuchElementException("Survivor leader is missing for playerId=$playerId")
    }

    fun getAllSurvivors(): List<Survivor> = survivors

    fun getIndexOfSurvivor(srvId: String?): Int {
        val idx = survivors.indexOfFirst { it.id == srvId }
        if (idx == -1) throw NoSuchElementException("Couldn't find survivor of id=$srvId for player=$playerId")
        return idx
    }

    suspend fun addNewSurvivor(survivor: Survivor): Report {
        return survivorRepository.addSurvivor(playerId, survivor)
            .onFailure {
                Fancam.error(it, tag = "survivor") { "Error on addNewSurvivor" }
            }
            .onSuccess {
                survivors.add(survivor)
            }
            .toReport()
    }


    suspend fun updateSurvivor(
        srvId: String, updateAction: suspend (Survivor) -> Survivor
    ): Report {
        val idx = getIndexOfSurvivor(srvId)
        val update = updateAction(survivors[idx])
        return survivorRepository.updateSurvivor(playerId, srvId, update)
            .onFailure {
                Fancam.error(it, tag = "survivor") { "Error on updateSurvivor" }
            }
            .onSuccess {
                survivors[idx] = update
            }
            .toReport()
    }

    override suspend fun debut(scope: PlayerScope): Result<Unit> {
        return runCatching {
            this.playerId = scope.playerId
            val _survivors = survivorRepository.getSurvivors(playerId).getOrThrow()

            if (_survivors.isEmpty()) {
                Fancam.warn(tag = "survivor") { "Survivor for playerId=$playerId is empty" }
            }
            survivors.addAll(_survivors)
        }
    }

    override suspend fun disband(scope: PlayerScope): Result<Unit> {
        return Result.success(Unit)
    }
}
