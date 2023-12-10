package com.squidsentry.mobile.data.model

import com.google.firebase.auth.AuthCredential

/**
 * Data class that captures user information for logged in users retrieved from GoogleLoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String,
    val credential: String
)