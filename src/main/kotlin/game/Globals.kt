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
    const val ADMIN_PLAYER_ID = "adm-id-123456"
    const val ADMIN_USERNAME = "givemeadmin"
    const val ADMIN_EMAIL = "admin@admin.com"
    const val ADMIN_PASSWORD = "admin"
    const val ADMIN_HASHED_PASSWORD = $$"$2a$10$Gcd5xsUf9FtkezEw84hgCezvUMu0Yk2nt6NGAzknk7s06OuV3pbou"
    const val ADMIN_TOKEN = "admin-token-123456"

    const val PLAYER_DATA_KEY = "admkey"
    const val PLAYER_SRV_ID = "srv-player"
    const val PLAYER_LEADER_TITLE = "MercifulLeader"
    const val PLAYER_LEVEL = 60
    const val PLAYER_EXP = 100
    const val FIGHTER_SRV_ID = "srv-fighter-m"
    const val RECON_SRV_ID = "srv-recon-f"
    const val PLAYER_WEP_ID = "deagle-replica"
    const val FIGHTER_WEP_ID = "bladesaw"
    const val RECON_WEP_ID = "fal3"
}