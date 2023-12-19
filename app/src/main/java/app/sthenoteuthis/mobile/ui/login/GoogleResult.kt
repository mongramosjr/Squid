package app.sthenoteuthis.mobile.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class GoogleResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)


