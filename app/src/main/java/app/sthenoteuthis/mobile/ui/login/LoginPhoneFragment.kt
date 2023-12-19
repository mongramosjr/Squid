package app.sthenoteuthis.mobile.ui.login

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import app.sthenoteuthis.mobile.databinding.FragmentLoginPhoneBinding

import app.sthenoteuthis.mobile.R

class LoginPhoneFragment : Fragment() {

    companion object {
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    }

    private lateinit var emailPhoneloginViewModel: EmailPhoneLoginViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    private var _binding: FragmentLoginPhoneBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginPhoneBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedStateHandle = findNavController(this).previousBackStackEntry!!.savedStateHandle
        savedStateHandle[LOGIN_SUCCESSFUL] = false

        emailPhoneloginViewModel = ViewModelProvider(requireActivity())[EmailPhoneLoginViewModel::class.java]

        val phoneNumberEditText = binding.phoneNumber
        val verifyCodeButton = binding.verifyCode
        val loadingProgressBar = binding.loading

        emailPhoneloginViewModel.codeloginFormState.observe(viewLifecycleOwner,
            Observer { codeloginFormState ->
                if (codeloginFormState == null) {
                    return@Observer
                }
                verifyCodeButton.isEnabled = codeloginFormState.isDataValid
                codeloginFormState.codeError?.let {
                    phoneNumberEditText.error = getString(it)
                }
            })

        emailPhoneloginViewModel.codeResult.observe(viewLifecycleOwner,
            Observer { codeResult ->
                codeResult ?: return@Observer
                loadingProgressBar.visibility = View.GONE
                codeResult.error?.let {
                    showLoginFailed(it)
                }
                codeResult.success?.let {
                    updateUiWithUser(it)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                emailPhoneloginViewModel.codeDataChanged(
                    phoneNumberEditText.text.toString(),
                )
            }
        }
        phoneNumberEditText.addTextChangedListener(afterTextChangedListener)

        verifyCodeButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            emailPhoneloginViewModel.verifyCode(
                phoneNumberEditText.text.toString(),
            )
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()

        //TEST
        savedStateHandle[LOGIN_SUCCESSFUL] = true
        //findNavController(this).popBackStack()
        //findNavController(this).navigate(R.id.action_loginCodeFragment_to_myselfFragment)

    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}