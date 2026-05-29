package game.domain.compound.model

import encore.datastore.collection.PlayerObjects
import game.domain.model.game.data.EffectCollection
import game.domain.model.game.data.TaskCollection
import game.domain.survivor.model.Morale
import game.domain.survivor.model.SurvivorCollection
import kotlinx.serialization.Serializable

@Serializable
data class CompoundData(
    val player: PlayerObjects?,
    val buildings: BuildingCollection = BuildingCollection(),
    val resources: GameResources = GameResources(),
    val survivors: SurvivorCollection = SurvivorCollection(),
    val tasks: TaskCollection = TaskCollection(),
    val effects: EffectCollection = EffectCollection(),
    val globalEffects: EffectCollection = EffectCollection(),
    val morale: Morale = Morale(),
    val moraleFilter: List<String> = listOf("food", "water", "security", "comfort")
)
