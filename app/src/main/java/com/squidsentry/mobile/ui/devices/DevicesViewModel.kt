package com.squidsentry.mobile.ui.devices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DevicesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "List of Devices"
    }
    val text: LiveData<String> = _text
}