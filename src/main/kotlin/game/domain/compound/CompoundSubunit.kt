package game.domain.compound

import encore.datastore.collection.PlayerId
import encore.definition.GameReference
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.PlayerScope
import encore.time.TimeCenter
import encore.utils.types.Outcome
import encore.utils.types.Report
import encore.utils.types.onFail
import encore.utils.types.toReport
import game.definition.ItemsDefinition
import game.domain.compound.model.*
import game.domain.items.model.Item
import io.ktor.util.date.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CompoundSubunit(private val compoundRepository: CompoundRepository) : Subunit<PlayerScope> {
    private lateinit var resources: GameResources
    private val buildings = mutableListOf<BuildingLike>()
    private val lastResourceValueUpdated = mutableMapOf<String, Long>()
    private lateinit var playerId: PlayerId

    fun getResources() = resources
    fun getBuildings() = buildings

    fun getBuildingById(bldId: String): BuildingLike {
        return buildings[getIndexOfBuilding(bldId)]
    }

    private fun getIndexOfBuilding(bldId: String): Int {
        val idx = buildings.indexOfFirst { it.id == bldId }
        if (idx == -1) throw NoSuchElementException("Building bldId=$bldId not found for playerId=$playerId")
        return idx
    }

    suspend fun updateBuilding(
        bldId: String,
        updateAction: suspend (BuildingLike) -> BuildingLike
    ): Report {
        val idx = getIndexOfBuilding(bldId)
        val update = updateAction(buildings[idx])
        return compoundRepository.updateBuilding(playerId, bldId, update)
            .onFailure {
                Fancam.error(it, tag = "compound") { "Error on updateBuilding" }
            }
            .onSuccess {
                buildings[idx] = update
            }
            .toReport()
    }

    suspend fun createBuilding(createAction: suspend () -> (BuildingLike)): Report {
        val create = createAction()
        return compoundRepository.createBuilding(playerId, create)
            .onFailure {
                Fancam.error(it, tag = "compound") { "Error on createBuilding" }
            }
            .onSuccess {
                this.buildings.add(create)
            }
            .toReport()
    }

    suspend fun deleteBuilding(bldId: String): Report {
        return compoundRepository.deleteBuilding(playerId, bldId)
            .onFailure {
                Fancam.error(it, tag = "compound") { "Error on deleteBuilding" }
            }
            .onSuccess {
                this.buildings.removeIf { it.id == bldId }
            }
            .toReport()
    }

    // probably means canceling build, hence deleting the ongoing createBuiding
    suspend fun cancelBuilding(bldId: String): Report {
        return compoundRepository.deleteBuilding(playerId, bldId)
            .onFailure {
                Fancam.error(it, tag = "compound") { "Error on cancelBuilding" }
            }
            .onSuccess {
                buildings.removeIf { it.id == bldId }
            }
            .toReport()
    }

    suspend fun collectBuilding(bldId: String): Outcome<GameResources> {
        val lastUpdate = lastResourceValueUpdated[bldId]
            ?: throw NoSuchElementException("Building bldId=$bldId is not categorized as production buildings")

        val collectedAmount = calculateResource(lastUpdate.seconds)
        lastResourceValueUpdated[bldId] = TimeCenter.now()

        lateinit var prod: String
        // updateBuilding already do necessary result catch and update the in-memory buildings
        updateBuilding(bldId) { oldBld ->
            // lookup to GameDefinitions, what does the building produce in 'prod' element
            prod = "wood"
            oldBld.copy(resourceValue = 0.0)
        }.onFail { return Outcome.Fail }

        val res = when (prod) {
            "wood" -> GameResources(wood = collectedAmount.toInt())
            "metal" -> GameResources(metal = collectedAmount.toInt())
            "cloth" -> GameResources(cloth = collectedAmount.toInt())
            "food" -> GameResources(food = collectedAmount.toInt())
            "water" -> GameResources(water = collectedAmount.toInt())
            "cash" -> GameResources(cash = collectedAmount.toInt())
            "ammunition" -> GameResources(ammunition = collectedAmount.toInt())
            else -> {
                throw IllegalArgumentException("Error during collectBuilding, type $prod doesn't exist")
            }
        }

        return Outcome.Ok(res)
    }

    suspend fun recycleBuilding(bldId: String): List<Item> {
        deleteBuilding(bldId)
        // lookup to building xml what should be obtained
        return emptyList()
    }

    suspend fun updateResources(updateAction: suspend (GameResources) -> GameResources): Report {
        val update = updateAction(resources)
        return compoundRepository.updateGameResources(playerId, update)
            .onFailure {
                Fancam.error(it, tag = "compound") { "Error on updateResources" }
            }
            .onSuccess {
                this.resources = update
            }
            .toReport()
    }

    fun calculateResource(durationSec: Duration): Double {
        val productionRate = 4
        // parameter: building level, effects
        // see GameDefintions of building.xml for realistic result
        return 10.0 + (productionRate * durationSec.inWholeMinutes)
    }

    override suspend fun debut(scope: PlayerScope): Result<Unit> {
        return runCatching {
            this.playerId = scope.playerId
            val _resources = compoundRepository.getGameResources(playerId).getOrThrow()
            val _buildings = compoundRepository.getBuildings(playerId).getOrThrow()
            resources = _resources
            buildings.addAll(_buildings)

            val now = getTimeMillis()

            for (bldLike in buildings) {
                if (GameReference.get<ItemsDefinition>().isResourceItem(bldLike.type)) {
                    lastResourceValueUpdated[bldLike.id] = now
                }
            }
        }
    }

    override suspend fun disband(scope: PlayerScope): Result<Unit> {
        return runCatching {
            val now = getTimeMillis()

            for (bldLike in buildings) {
                val lastUpdate = lastResourceValueUpdated[bldLike.id] ?: continue
                updateBuilding(bldLike.id) { oldBld ->
                    oldBld.copy(resourceValue = calculateResource((now - lastUpdate).seconds))
                }
            }
        }
    }
}
