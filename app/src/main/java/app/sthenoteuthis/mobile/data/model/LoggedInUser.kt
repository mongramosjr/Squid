package app.sthenoteuthis.mobile.data.model

/**
 * Data class that captures user information for logged in users retrieved from GoogleLoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String,
    val credential: String
)