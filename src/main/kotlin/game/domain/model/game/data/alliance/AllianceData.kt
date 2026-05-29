package game.domain.model.game.data.alliance

import game.domain.model.game.data.effects.Effect
import kotlinx.serialization.Serializable

@Serializable
data class AllianceData(
    val allianceDataSummary: AllianceDataSummary,
    val members: AllianceMemberList?,
    val messages: AllianceMessageList?,
    val enemies: AllianceList?,
    val ranks: Map<String, Int>?,
    val bannerEdits: Int,
    val effects: List<Effect>?,
    val tokens: Int?,
    val taskSet: Int?,
    val tasks: Map<String, Int>?, // string as key, but parsed to int
    val attackedTargets: Map<String, TargetRecord>?,
    val scoutedTargets: Map<String, TargetRecord>?
)
