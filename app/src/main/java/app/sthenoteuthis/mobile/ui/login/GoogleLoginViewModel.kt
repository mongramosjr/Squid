package app.sthenoteuthis.mobile.ui.login



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import app.sthenoteuthis.mobile.R
import app.sthenoteuthis.mobile.data.model.LoggedInUser
import android.util.Log


class GoogleLoginViewModel: ViewModel() {
    private val _googleResult = MutableLiveData<GoogleResult>()
    val googleResult: LiveData<GoogleResult> = _googleResult

    fun login(completedTask: Task<GoogleSignInAccount>) {
        // can be launched in a separate asynchronous job


        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Google Sign-in was successful, get user details
            val googleToken = account?.idToken // You can use this token for server-side authentication
            val googleUserName = account?.displayName // Get user's display name
            val googleEmail = account?.email // Get user's email

            val googleUser = LoggedInUser(googleEmail!!, googleUserName!!, googleToken!!)

            _googleResult.value =
                GoogleResult(success = LoggedInUserView(displayName = googleUser.displayName))

            // Perform your actions after successful Google Sign-In
            // Example: You might want to send the Google token to your server for authentication

            Log.e("GoogleSignIn", "Google sign-in succesful.")
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Google sign-in failed: ${e.statusCode}")
            _googleResult.value = GoogleResult(error = R.string.login_failed)
        }
    }

    fun logout(){
        _googleResult.value = GoogleResult(error = -1)
    }
}