package app.sthenoteuthis.mobile.ui.myself

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyselfViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Me"
    }
    val text: LiveData<String> = _text
}