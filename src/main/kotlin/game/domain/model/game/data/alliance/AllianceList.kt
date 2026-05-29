package game.domain.model.game.data.alliance

import kotlinx.serialization.Serializable

@Serializable
data class AllianceList(
    val list: List<AllianceDataSummary> = listOf()
)
