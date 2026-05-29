package game.domain.survivor.model

import kotlinx.serialization.Serializable

@Serializable
data class SurvivorCollection(
    val list: List<Survivor> = listOf()
) {
    companion object {
        fun playerOnly(): List<Survivor> {
            return listOf(
                Survivor.playerM(),
            )
        }

        fun threeSurvivors(): List<Survivor> {
            return listOf(
                Survivor.playerM(),
                Survivor.fighterM(),
                Survivor.reconF(),
            )
        }
    }
}
