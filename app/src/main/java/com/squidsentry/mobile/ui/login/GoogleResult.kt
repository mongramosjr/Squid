package com.squidsentry.mobile.ui.login

import com.google.firebase.auth.GoogleAuthProvider

/**
 * Authentication result : success (user details) or error message.
 */
data class GoogleResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)


