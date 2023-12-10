package com.squidsentry.mobile.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class EmailPhoneResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)