package testUtils

import encore.account.model.Profile
import encore.datastore.collection.PlayerAccount
import encore.datastore.collection.PlayerId
import encore.time.TimeCenter
import encore.utils.hash

fun createAccount(playerId: PlayerId, username: String, password: String): PlayerAccount {
    return PlayerAccount(
        playerId = playerId,
        username = username,
        hashedPassword = hash(password),
        profile = createProfile(playerId, username)
    )
}

fun createProfile(playerId: PlayerId, username: String): Profile {
    val now = TimeCenter.system.now()
    return Profile(
        playerId = playerId,
        createdAt = now,
        lastLogin = now,
        displayName = username,
        avatarUrl = "xiaotinggg"
    )
}
