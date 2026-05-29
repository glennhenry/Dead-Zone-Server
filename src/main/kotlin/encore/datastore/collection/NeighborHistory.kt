package encore.datastore.collection

import game.domain.model.network.RemotePlayerData

/**
 * Neighbor history table
 */
data class NeighborHistory(
    val playerId: PlayerId, // reference to UserDocument
    val map: Map<String, game.domain.model.network.RemotePlayerData>? = emptyMap()
) {
    companion object {
        fun empty(pid: String): NeighborHistory {
            return NeighborHistory(
                playerId = pid,
                map = emptyMap()
            )
        }
    }
}
