package app.sthenoteuthis.mobile.ui.login

/**
 * Data validation state of the login form.
 */
data class EmailPhoneLoginFormState(
    val emailorphoneError: Int? = null,
    val isDataValid: Boolean = false
)