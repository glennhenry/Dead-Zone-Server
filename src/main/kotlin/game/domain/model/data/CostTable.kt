package game.domain.model.data

import kotlinx.serialization.Serializable

@Serializable
data class CostTable(
    val objectsByKey: Map<String, CostEntry> = mapOf(),
    val categories: Map<String, Map<String, CostEntry>> = mapOf()
)
