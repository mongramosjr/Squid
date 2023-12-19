package app.sthenoteuthis.mobile.ui.signup

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import app.sthenoteuthis.mobile.R
import app.sthenoteuthis.mobile.databinding.FragmentSignupBinding
import app.sthenoteuthis.mobile.ui.ProgressFragment
import app.sthenoteuthis.mobile.ui.viewmodel.FirebaseViewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignupFragment : ProgressFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentSignupBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var firebaseViewModel: FirebaseViewModel

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private const val TAG = "EmailPasswordSignup"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate ")

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        firebaseViewModel = ViewModelProvider(requireActivity())[FirebaseViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView ")

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_signup, container, false)
        _binding = FragmentSignupBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        binding.signup.setOnClickListener{
            Log.d(TAG, "sending createUserWithEmailAndPassword ")
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            createAccount(email, password)
        }
        binding.loginLink.setOnClickListener {
            Navigation.findNavController(binding.root).navigate(R.id.action_signupFragment_to_loginFragment)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm(true)) {
            return
        }

        showProgressBar()

        firebaseViewModel.auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = firebaseViewModel.auth!!.currentUser
                    //TODO:  save the user data to local sqlite
                    Navigation.findNavController(binding.root).navigate(R.id.action_signupFragment_to_myselfFragment)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                hideProgressBar()
            }
    }

    private fun validateForm(isConfirmPassword: Boolean = false): Boolean {
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

        if(isConfirmPassword){
            val confirmPassword = binding.confirmPassword.text.toString()
            if (TextUtils.isEmpty(confirmPassword)) {
                binding.confirmPassword.error = "Required."
                valid = false
            } else {
                binding.confirmPassword.error = null
            }

            if(password!=confirmPassword){
                binding.confirmPassword.error = "Confirm password not matched."
                valid = false
            }else{
                binding.confirmPassword.error = null
            }
        }

        return valid
    }


}