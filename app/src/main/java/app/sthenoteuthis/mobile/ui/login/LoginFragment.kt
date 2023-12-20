package app.sthenoteuthis.mobile.ui.login


import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import app.sthenoteuthis.mobile.R
import app.sthenoteuthis.mobile.databinding.FragmentLoginBinding
import app.sthenoteuthis.mobile.ui.ProgressFragment
import app.sthenoteuthis.mobile.ui.viewmodel.FirebaseViewModel
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.oAuthProvider


class LoginFragment : ProgressFragment() {

    private var _binding: FragmentLoginBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var firebaseViewModel: FirebaseViewModel

    private lateinit var googleSignInClient: SignInClient
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        signinResultGoogle(result.data)
    }

    private lateinit var fbCallbackManager: CallbackManager

    companion object {
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
        private const val RC_MULTI_FACTOR = 9005
        private const val TAG = "SquidLogin"

        private val PROVIDER_MAP = mapOf(
            "Apple" to "apple.com",
            "Microsoft" to "microsoft.com",
            "Yahoo" to "yahoo.com",
            "Twitter" to "twitter.com",
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate ")
        firebaseViewModel = ViewModelProvider(requireActivity())[FirebaseViewModel::class.java]
        //fbCallbackManager = CallbackManager.Factory.create()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        setProgressBar(binding.progressBar)

        val googleSigninButton = binding.googleSigninButton
        val twitterSigninButton = binding.twitterSigninButton
        val facebookSigninButton = binding.facebookSigninButton

        // Initialize Facebook Login button
        fbCallbackManager = CallbackManager.Factory.create()

        // Configure Google Sign In
        googleSignInClient = Identity.getSignInClient(requireContext())

        googleSigninButton.setOnClickListener {
            signInGoogle()
        }
        twitterSigninButton.setOnClickListener {
            signInTwitter()
        }
        facebookSigninButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this,
                fbCallbackManager, listOf("email", "public_profile"))
        }

        LoginManager.getInstance().registerCallback(
        //facebookSigninButton.registerCallback(
            fbCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$result")
                    firebaseAuthWithFacebook(result.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                    //updateUI(null)
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                    //updateUI(null)
                }
            },
        )

        binding.login.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            signInEmailPassword(email, password)
        }

        binding.createLink.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(R.id.action_loginFragment_to_signupFragment)
        }


    }
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseViewModel.auth?.currentUser
        //updateUI(currentUser)

        // Look for a pending auth result
        val pending = firebaseViewModel.auth?.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                Log.d(TAG, "checkPending:onSuccess:$authResult")
                //updateUI(authResult.user)
            }.addOnFailureListener { e ->
                Log.w(TAG, "checkPending:onFailure", e)
            }
        } else {
            Log.d(TAG, "checkPending: null")
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signInEmailPassword(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateFormEmailPassword()) {
            return
        }

        showProgressBar()

        firebaseViewModel.auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = firebaseViewModel.auth?.currentUser
                    Toast.makeText(
                        context,
                        "Welcome, $user",
                        Toast.LENGTH_SHORT,
                    ).show()
                    Navigation.findNavController(binding.root).navigate(R.id.action_loginFragment_to_myselfFragment)
                    hideProgressBar()
                    return@addOnCompleteListener
                }
                if (task.exception is FirebaseAuthMultiFactorException) {
                    checkForMultiFactorFailure(task.exception!!)
                }else if (!task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                hideProgressBar()
            }
    }

    private fun checkForMultiFactorFailure(e: Exception) {
        // Multi-factor authentication with SMS is currently only available for
        // Google Cloud Identity Platform projects. For more information:
        // https://cloud.google.com/identity-platform/docs/android/mfa
        if (e is FirebaseAuthMultiFactorException) {
            Log.w(TAG, "multiFactorFailure", e)
            val resolver = e.resolver

            val args = bundleOf(
                MultiFactorSignInFragment.EXTRA_MFA_RESOLVER to resolver
            )
            Navigation.findNavController(binding.root).navigate(R.id.action_loginFragment_to_signupFragment, args)
        }
    }



    private fun validateFormEmailPassword(): Boolean {
        var valid = true

        val email = binding.email.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.email.error = "Required."
            valid = false
        } else {
            binding.email.error = null
        }

        val password = binding.password.text.toString()

        if (TextUtils.isEmpty(password)) {
            binding.password.error = "Required."
            valid = false
        } else {
            binding.password.error = null
        }

        return valid
    }

    private fun signInGoogle() {
        val signInRequest = GetSignInIntentRequest.builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        googleSignInClient.getSignInIntent(signInRequest)
            .addOnSuccessListener { pendingIntent ->
                launchSignInGoogle(pendingIntent)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "signInGoogle Sign-in failed", e)
                Toast.makeText(
                    context,
                    "Google Authentication Failed",
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }
    private fun oneTapSignInGoogle() {
        // Configure One Tap UI
        val oneTapRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build(),
            )
            .build()

        // Display the One Tap UI
        googleSignInClient.beginSignIn(oneTapRequest)
            .addOnSuccessListener { result ->
                launchSignInGoogle(result.pendingIntent)
            }
            .addOnFailureListener { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.e(TAG, "oneTapSignInGoogle Sign-in failed", e)
                Toast.makeText(
                    context,
                    "Google Authentication Failed",
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    private fun launchSignInGoogle(pendingIntent: PendingIntent) {
        try {
            val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent)
                .build()
            googleSignInLauncher.launch(intentSenderRequest)
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "launchSignInGoogle couldn't start Sign In: ${e.localizedMessage}")
            Toast.makeText(
                context,
                "Google Authentication Failed",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun signinResultGoogle(data: Intent?) {
        // Result returned from launching the Sign In PendingIntent
        try {
            // Google Sign In was successful, authenticate with Firebase
            val credential = googleSignInClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                Log.d(TAG, "signinResultGoogle: ${credential.id}")
                firebaseAuthWithGoogle(idToken)
            } else {
                // Shouldn't happen.
                Log.d(TAG, "No ID token!")
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "signinResultGoogle sign in failed", e)
            Toast.makeText(
                context,
                "Google Authentication Failed",
                Toast.LENGTH_SHORT,
            ).show()
            //updateUI(null)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showProgressBar()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseViewModel.auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "firebaseAuthWithGoogle:success")
                    val user = firebaseViewModel.auth?.currentUser
                    Toast.makeText(
                        context,
                        "Welcome, ${user?.displayName}",
                        Toast.LENGTH_SHORT,
                    ).show()
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "firebaseAuthWithGoogle:failure", task.exception)
                    //val view = binding.mainLayout
                    //Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    //updateUI(null)
                    Toast.makeText(
                        context,
                        "Google Authentication Failed",
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                hideProgressBar()
            }
    }
    fun onFacebookLoginClick(view: android.view.View) {
        LoginManager.getInstance().logInWithReadPermissions(this, fbCallbackManager, listOf("email", "public_profile"))
    }
    private fun firebaseAuthWithFacebook(token: AccessToken) {
        Log.d(TAG, "firebaseAuthWithFacebook:$token")
        showProgressBar()

        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseViewModel.auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    //val user = auth.currentUser
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    //updateUI(null)
                }

                hideProgressBar()
            }
    }

    private fun signInTwitter() {
        // Could add custom scopes here
        val customScopes = ArrayList<String>()

        // Examples of provider ID: apple.com (Apple), microsoft.com (Microsoft), yahoo.com (Yahoo)
        val providerId = getProviderId()

        firebaseViewModel.auth?.startActivityForSignInWithProvider(
            requireActivity(),
            oAuthProvider(providerId, firebaseViewModel.auth!!) {
                scopes = customScopes
            },
        )
            ?.addOnSuccessListener { authResult ->
                Log.d(TAG, "signInTwitter:onSuccess:${authResult.user}")
                //updateUI(authResult.user)
            }
            ?.addOnFailureListener { e ->
                Log.w(TAG, "signInTwitter:onFailure", e)
                Toast.makeText(
                    context,
                    "Twitter Authentication failed.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
    }

    private fun getProviderId(): String {
        val providerName = "Twitter" //spinnerAdapter.getItem(binding.providerSpinner.selectedItemPosition)
        return PROVIDER_MAP[providerName!!] ?: error("No provider selected")
    }




}