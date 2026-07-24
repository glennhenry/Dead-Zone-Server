package game

/**
 * Contains any uncategorized global application constants.
 *
 * Includes:
 * - Fixed admin's data and credentials.
 * - Dummy data ready to use for game logic.
 */
object Globals {
    const val ADMIN_RESERVED_NAME = "givemeadmin"
    const val ADMIN_PLAYER_ID = "ADM-ID-123456"
    const val ADMIN_USERNAME = "givemeadmin"
    const val ADMIN_EMAIL = "admin@admin.com"
    const val ADMIN_PASSWORD = "admin"
    const val ADMIN_HASHED_PASSWORD = $$"$2a$10$Gcd5xsUf9FtkezEw84hgCezvUMu0Yk2nt6NGAzknk7s06OuV3pbou"
    const val ADMIN_TOKEN = "admin-token-123456"

    const val PLAYER_DATA_KEY = "admkey"
    const val PLAYER_SRV_ID = "SRV-PLAYER"
    const val PLAYER_LEADER_TITLE = "MercifulLeader"
    const val PLAYER_LEVEL = 60
    const val PLAYER_EXP = 100
    const val FIGHTER_SRV_ID = "SRV-FIGHTER-M"
    const val RECON_SRV_ID = "SRV-RECON-F"
    const val PLAYER_WEP_ID = "DEAGLE-REPLICA"
    const val FIGHTER_WEP_ID = "BLADESAW"
    const val RECON_WEP_ID = "FAL3"
}