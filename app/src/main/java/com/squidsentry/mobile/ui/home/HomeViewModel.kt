package com.squidsentry.mobile.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squidsentry.mobile.ThingSpeak
import com.squidsentry.mobile.ThingSpeakApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
    
    private val _thingSpeakData = MutableLiveData<ThingSpeak?>()
    val thingSpeakData: LiveData<ThingSpeak?> get() = _thingSpeakData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    private var errorMessage: String = ""

    fun getThingSpeakData(){

        _isLoading.value = true
        _isError.value = false

        val client = ThingSpeakApiClient.apiService.getLast(100)

        client.enqueue(object : Callback<ThingSpeak> {

            override fun onResponse(call: Call<ThingSpeak>,
                                    response: Response<ThingSpeak>) {
                if (response.isSuccessful) {  
                    val responseBody = response.body()
                    if (responseBody == null) {
                        onError("Data Processing Error")
                        return
                    }
                    _isLoading.value = false
                    //_thingSpeakData.postValue(responseBody)
                    _thingSpeakData.value = responseBody
                    Log.i("MMMMMMMM", "Processing Response from ThingSpeak")
                } else {
                    // Handle error
                    print("${response.errorBody()} ")
                    onError("Data Processing Error")
                    return
                }
            }

            override fun onFailure(call: Call<ThingSpeak>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }
        })

    }

    private fun onError(inputMessage: String?) {

        val message = if (inputMessage.isNullOrBlank() or inputMessage.isNullOrEmpty()) "Unknown Error"
        else inputMessage

        errorMessage = StringBuilder("ERROR: ")
            .append("$message some data may not displayed properly").toString()

        _isError.value = true
        _isLoading.value = false
    }
}