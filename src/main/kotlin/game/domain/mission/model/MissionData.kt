package game.domain.mission.model

import encore.datastore.collection.PlayerId
import game.domain.model.game.data.TimerData
import kotlinx.serialization.Serializable
import game.domain.survivor.model.SurvivorData

@Serializable
data class MissionData(
    val id: String,
    val player: SurvivorData,
    val stats: MissionStats? = null,
    val xpEarned: Int,
    val xp: Map<String, Int> = emptyMap(),
    val completed: Boolean,
    val assignmentId: String,
    val assignmentType: String,
    val playerId: PlayerId? = null,
    val compound: Boolean = false,
    val areaLevel: Int = 1,
    val areaId: String = "",
    val type: String = "",
    val suburb: String = "",
    val automated: Boolean = false,
    val survivors: List<Map<String, String>> = emptyList(), //survivor ids to mission ids
    val srvDown: List<Map<String, String>> = emptyList(), //survivor ids
    val buildingsDestroyed: List<String> = emptyList(), //building ids
    val returnTimer: TimerData? = null,
    val lockTimer: TimerData? = null,
    val loot: List<game.domain.items.model.Item> = emptyList(),
    val highActivityIndex: Int? = null
) {
    companion object {
        fun dummy(srvId: String): MissionData {
            return MissionData(
                id = "mission001",
                player = SurvivorData.dummy(srvId),
                xpEarned = 50,
                xp = mapOf("mission" to 50),
                completed = false,
                assignmentId = "compoundTutorial",
                assignmentType = "tutorial",
                compound = true,
                type = "compound",
                survivors = listOf(mapOf("id" to srvId))
            )
        }
    }
}
