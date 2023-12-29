package app.sthenoteuthis.mobile.ui.myself

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.sthenoteuthis.mobile.R
import app.sthenoteuthis.mobile.data.SquidDatabase
import app.sthenoteuthis.mobile.data.model.LoggedInAccountDao
import app.sthenoteuthis.mobile.data.model.toLoggedInAccount
import app.sthenoteuthis.mobile.databinding.FragmentMyselfBinding
import app.sthenoteuthis.mobile.ui.ProgressFragment
import app.sthenoteuthis.mobile.ui.viewmodel.FirebaseViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.BuildConfig
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyselfFragment : ProgressFragment() {

    private var _binding: FragmentMyselfBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var firebaseViewModel: FirebaseViewModel

    private lateinit var loggedInAccountDao: LoggedInAccountDao

    // Build FirebaseUI sign in intent. For documentation on this operation and all
    // possible customization see: https://github.com/firebase/firebaseui-android
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { result -> this.onSignInResult(result) }

    private var beenHere: Boolean = false

    companion object {
        private const val TAG = "SquidMultipleLogin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.w(TAG, "onCreate")
        firebaseViewModel = ViewModelProvider(requireActivity())[FirebaseViewModel::class.java]

        val database = SquidDatabase.getDatabase(requireContext())
        loggedInAccountDao = database.loggedInAccountDao()

        hasBeenHere()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        //val view = inflater.inflate(R.layout.fragment_myself, container, false)
        _binding = FragmentMyselfBinding.inflate(inflater, container, false)
        Log.w(TAG, "onCreateView")
        hideButtons()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.w(TAG, "onViewCreated")
        binding.btnToLogin.setOnClickListener { startSignIn() }
        binding.btnToLogout.setOnClickListener { signOut() }
    }

    override fun onStart() {
        super.onStart()
        Log.w(TAG, "onStart")
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseViewModel.auth?.currentUser
        if (currentUser != null) {
            reload()
        }else{
            updateUI(null)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            // Sign in succeeded
            updateUI(firebaseViewModel.auth?.currentUser)
            // Store to the local userdatabase
            firebaseViewModel.auth?.currentUser?.let { saveNewLogin(it) }
        } else {
            // Sign in failed
            Toast.makeText(context, "Sign In Failed", Toast.LENGTH_SHORT).show()
            updateUI(null)
        }
    }

    private fun startSignIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            AuthUI.IdpConfig.TwitterBuilder().build(),
        )
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setIsSmartLockEnabled(!BuildConfig.DEBUG)
            .setAvailableProviders(providers)
            .setLogo(R.mipmap.ic_launcher)
            .build()
        signInLauncher.launch(intent)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Signed in

            binding.textMyself.text = getString(R.string.account_title)
            binding.squidAccountId.text = user.email

            binding.btnToLogin.visibility = View.GONE
            binding.btnToLogout.visibility = View.VISIBLE
        } else {
            if(beenHere) { // Signed out
                binding.textMyself.text = getString(R.string.account_title_away)
            }else{ // no account has been login
                binding.textMyself.text = getString(R.string.account_title_new)
            }

            binding.squidAccountId.text = null

            binding.btnToLogin.visibility = View.VISIBLE
            binding.btnToLogout.visibility = View.GONE
        }
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(requireContext())
        updateUI(null)
    }

    private fun reload() {
        firebaseViewModel.auth?.currentUser!!.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateUI(firebaseViewModel.auth?.currentUser!!)
            } else {
                Log.e(TAG, "reload", task.exception)
                updateUI(null)
            }
        }
    }

    private fun hideButtons() {
        binding.btnToLogin.visibility = View.GONE
        binding.btnToLogout.visibility = View.GONE
    }

    private fun hasBeenHere() {
        CoroutineScope(Dispatchers.IO).launch {
            val count = loggedInAccountDao.size()
            if(count>0){
                beenHere = true
            }
        }
    }

    private fun saveNewLogin(firebaseUser: FirebaseUser){
        lifecycleScope.launch(Dispatchers.IO) {
            val loggedInAccount = firebaseUser.toLoggedInAccount()
            loggedInAccountDao.insert(loggedInAccount)
        }
    }

}