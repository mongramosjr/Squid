package com.squidsentry.mobile.data

import com.squidsentry.mobile.data.model.LoggedInUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class EmailPhoneLoginRepository(val dataSource: EmailPhoneLoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun login(emailorphone: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(emailorphone)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    fun verifyCode(code: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.verifyCode(code)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}