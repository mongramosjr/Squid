package com.squidsentry.mobile.ui.login

/**
 * Data validation state of the login form.
 */
data class CodeLoginFormState(
    val codeError: Int? = null,
    val isDataValid: Boolean = false
)