package game.domain.model.game.data.alliance

import kotlinx.serialization.Serializable

@Serializable
data class AllianceMemberList(
    val list: List<AllianceMember>
)
