package app.sthenoteuthis.mobile.data

import app.sthenoteuthis.mobile.data.model.LoggedInUser
import java.io.IOException
import java.util.UUID

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class EmailPhoneLoginDataSource {

    fun verifyCode(code: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(UUID.randomUUID().toString())
            fakeUser.displayName = "Jane Doe"
            fakeUser.email = "janedoe@email.com"
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }
    fun login(emailorphone: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(UUID.randomUUID().toString())
            fakeUser.displayName = "Jane Doe"
            fakeUser.email = "janedoe@email.com"
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}