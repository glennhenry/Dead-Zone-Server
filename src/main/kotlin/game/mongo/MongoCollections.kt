package game.mongo

/**
 * Encompasses the Mongo collection name of the 4 base collections
 * and user defined.
 */
data class MongoCollections(
    val playerAccount: String,
    val playerObjects: String,
    val neighborHistory: String,
    val inventory: String,
    val playerServerObjects: String,
    val serverObjects: String
)

/**
 * The Mongo collection names for runtime (non-test).
 */
val RuntimeMongoCollections = MongoCollections(
    playerAccount = "player_account",
    playerObjects = "player_objects",
    neighborHistory = "neighbor_history",
    inventory = "inventory",
    playerServerObjects = "player_server_objects",
    serverObjects = "server_objects"
)
