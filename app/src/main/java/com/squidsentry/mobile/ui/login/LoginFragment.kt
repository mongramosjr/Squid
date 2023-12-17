package com.squidsentry.mobile.ui.login

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.squidsentry.mobile.R
import com.squidsentry.mobile.databinding.FragmentLoginBinding
import com.squidsentry.mobile.ui.ProgressFragment
import com.squidsentry.mobile.ui.viewmodel.FirebaseViewModel


class LoginFragment : ProgressFragment() {

    private var _binding: FragmentLoginBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var firebaseViewModel: FirebaseViewModel

    companion object {
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
        private const val RC_MULTI_FACTOR = 9005
        private const val TAG = "EmailPasswordLogin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate ")
        firebaseViewModel = ViewModelProvider(requireActivity())[FirebaseViewModel::class.java]

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

        googleSigninButton.setOnClickListener {
        }
        twitterSigninButton.setOnClickListener {
        }
        facebookSigninButton.setOnClickListener {
        }

        binding.login.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            signIn(email, password)
        }

        binding.createLink.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(R.id.action_loginFragment_to_signupFragment)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
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



    private fun validateForm(): Boolean {
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
}