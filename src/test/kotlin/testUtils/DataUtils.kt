package testUtils

import encore.account.model.Profile
import game.mongo.collection.PlayerAccount
import game.mongo.collection.PlayerId
import encore.time.TimeCenter
import encore.utils.hash

fun createAccount(playerId: PlayerId, username: String, password: String): PlayerAccount {
    return PlayerAccount(
        playerId = playerId,
        username = username,
        hashedPassword = hash(password),
        profile = createProfile(playerId)
    )
}

fun createProfile(playerId: PlayerId, username: String = "xyz"): Profile {
    val now = TimeCenter.now()
    return Profile(
        playerId = playerId,
        createdAt = now,
        lastLogin = now,
        displayName = username,
        avatarUrl = "xiaotinggg"
    )
}
