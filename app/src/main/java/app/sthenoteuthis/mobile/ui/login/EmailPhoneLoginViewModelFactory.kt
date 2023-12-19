package app.sthenoteuthis.mobile.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.sthenoteuthis.mobile.data.EmailPhoneLoginDataSource
import app.sthenoteuthis.mobile.data.EmailPhoneLoginRepository

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class EmailPhoneLoginViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmailPhoneLoginViewModel::class.java)) {
            return EmailPhoneLoginViewModel(
                loginRepository = EmailPhoneLoginRepository(
                    dataSource = EmailPhoneLoginDataSource()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}