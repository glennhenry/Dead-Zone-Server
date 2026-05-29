package game.domain.compound

import encore.datastore.collection.PlayerId
import encore.fancam.Fancam
import encore.subunit.Subunit
import encore.subunit.scope.PlayerScope
import game.domain.compound.model.BuildingLike
import game.domain.compound.model.GameResources
import game.domain.compound.model.copy
import game.domain.compound.model.id
import game.domain.compound.model.type
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

    suspend fun updateResources(updateAction: suspend (GameResources) -> GameResources) {
        val update = updateAction(resources)
        val result = compoundRepository.updateGameResources(playerId, update)
        result.onFailure {
            Fancam.error(tag = "compound") { "Error on updateResources" }
        }
        result.onSuccess {
            this.resources = update
        }
    }

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
    ) {
        val idx = getIndexOfBuilding(bldId)
        val update = updateAction(buildings[idx])
        val result = compoundRepository.updateBuilding(playerId, bldId, update)
        result.onFailure {
            Fancam.error(tag = "compound") { "Error on updateBuilding" }
        }
        result.onSuccess {
            buildings[idx] = update
        }
    }

    suspend fun createBuilding(createAction: suspend () -> (BuildingLike)) {
        val create = createAction()
        val result = compoundRepository.createBuilding(playerId, create)
        result.onFailure {
            Fancam.error(tag = "compound") { "Error on createBuilding" }
        }
        result.onSuccess {
            this.buildings.add(create)
        }
    }

    suspend fun deleteBuilding(bldId: String) {
        val result = compoundRepository.deleteBuilding(playerId, bldId)
        result.onFailure {
            Fancam.error(tag = "compound") { "Error on deleteBuilding" }
        }
        result.onSuccess {
            this.buildings.removeIf { it.id == bldId }
        }
    }

    // probably means canceling build, hence deleting the ongoing createBuiding
    suspend fun cancelBuilding(bldId: String) {
        val result = compoundRepository.deleteBuilding(playerId, bldId)
        result.onFailure {
            Fancam.error(tag = "compound") { "Error on cancelBuilding" }
        }
        result.onSuccess {
            buildings.removeIf { it.id == bldId }
        }
    }

    suspend fun collectBuilding(bldId: String): GameResources {
        val lastUpdate = lastResourceValueUpdated[bldId]
            ?: throw NoSuchElementException("Building bldId=$bldId is not categorized as production buildings")

        val collectedAmount = calculateResource(lastUpdate.seconds)
        lastResourceValueUpdated[bldId] = getTimeMillis()

        lateinit var prod: String
        // updateBuilding already do necessary result catch and update the in-memory buildings
        updateBuilding(bldId) { oldBld ->
            // lookup to GameDefinitions, what does the building produce in 'prod' element
            prod = "wood"
            oldBld.copy(resourceValue = 0.0)
        }

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

        return res
    }

    suspend fun recycleBuilding(bldId: String): List<game.domain.items.model.Item> {
        deleteBuilding(bldId)
        // lookup to building xml what should be obtained
        return emptyList()
    }

    fun calculateResource(durationSec: Duration): Double {
        val productionRate = 4
        // parameter: building level, effects
        // see GameDefintions of building.xml for realistic result
        return 10.0 + (productionRate * durationSec.inWholeMinutes)
    }

    private fun isProductionBuilding(idInXML: String): Boolean {
        return idInXML.contains("resource")
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
                if (isProductionBuilding(bldLike.type)) {
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
