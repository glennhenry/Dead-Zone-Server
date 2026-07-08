package game.domain.login

import encore.time.TimeCenter
import game.domain.compound.model.Building
import game.domain.compound.model.BuildingLike
import game.domain.compound.model.GameResources
import game.domain.model.game.data.hasEnded
import kotlin.math.ceil
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

/**
 * Lazily update player's data based on timer or lastLogin
 */
object LazyDataUpdater {
    fun depleteResources(lastLogin: Long, res: GameResources): GameResources {
        val now = TimeCenter.now()
        val minutesPassed = max(0, (now - lastLogin).milliseconds.inWholeMinutes)
        val depletionRate = 0.01
        // TO-DO depletion should be based on the number of survivors
        // depletion formula right now: each minutes deplete res by 0.01, an hour is 0.6, ceil the result

        val depleted = ceil(depletionRate * minutesPassed).toInt()
        return res.copy(
            food = max(0, res.food - depleted),
            water = max(0, res.water - depleted)
        )
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    fun removeBuildingTimerIfDone(buildings: List<BuildingLike>): List<BuildingLike> {
        return buildings.map { bld ->
            if (bld is Building) {
                val upgradeWasGoing = bld.upgrade != null
                val repairWasGoing = bld.repair != null

                if (upgradeWasGoing) {
                    val upgradeDone = bld.upgrade.hasEnded()

                    if (upgradeDone) {
                        val level = (bld.upgrade.data?.get("level") as? Int ?: 1)
                        return@map bld.copy(level = level, upgrade = null)
                    } else {
                        return@map bld
                    }
                }

                if (repairWasGoing) {
                    val repairDone = bld.repair.hasEnded()

                    if (repairDone) {
                        return@map bld.copy(repair = null, destroyed = false)
                    } else {
                        return@map bld
                    }
                }

                bld
            } else {
                return@map bld
            }
        }
    }
}
