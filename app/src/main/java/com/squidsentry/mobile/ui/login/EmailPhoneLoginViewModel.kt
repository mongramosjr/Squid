package com.squidsentry.mobile.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.squidsentry.mobile.data.LoginRepository
import com.squidsentry.mobile.data.Result

import com.squidsentry.mobile.R
import com.squidsentry.mobile.data.EmailPhoneLoginRepository

class EmailPhoneLoginViewModel(private val loginRepository: EmailPhoneLoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<EmailPhoneLoginFormState>()
    val loginFormState: LiveData<EmailPhoneLoginFormState> = _loginForm

    private val _codeloginForm = MutableLiveData<CodeLoginFormState>()
    val codeloginFormState: LiveData<CodeLoginFormState> = _codeloginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _codeResult = MutableLiveData<CodeResult>()
    val codeResult: LiveData<CodeResult> = _codeResult

    fun login(emailorphone: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(emailorphone)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun verifyCode(code: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.verifyCode(code)

        if (result is Result.Success) {
            _codeResult.value =
                CodeResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _codeResult.value = CodeResult(error = R.string.login_failed)
        }
    }

    fun logout(){
        loginRepository.logout()
        _loginResult.value = LoginResult(error = -1)
        _codeResult.value = CodeResult(error = -1)
    }

    fun loginDataChanged(emailorphone: String) {
        if (!isEmailOrPhoneValid(emailorphone)) {
            _loginForm.value = EmailPhoneLoginFormState(emailorphoneError = R.string.invalid_username)
        }else {
            _loginForm.value = EmailPhoneLoginFormState(isDataValid = true)
        }
    }

    fun codeDataChanged(code: String) {
        if (!isCodeValid(code)) {
            _codeloginForm.value = CodeLoginFormState(codeError = R.string.invalid_code)
        } else {
            _codeloginForm.value = CodeLoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isEmailOrPhoneValid(emailorphone: String): Boolean {
        return if (emailorphone.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(emailorphone).matches()
        }else if(emailorphone.contains("^d{10}$")) {
            Patterns.PHONE.matcher(emailorphone).matches()
        }else {
            emailorphone.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isCodeValid(code: String): Boolean {
        return code.length == 6
    }
}