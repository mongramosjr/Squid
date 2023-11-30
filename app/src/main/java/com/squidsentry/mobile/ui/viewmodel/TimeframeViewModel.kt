package com.squidsentry.mobile.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Date

class TimeframeViewModel : ViewModel() {
    private val _timeframesDate = MutableLiveData<Date>()
    val timeframesDate: LiveData<Date> get() = _timeframesDate

    private val _currentTabPosition = MutableLiveData<Int>()
    val currentTabPosition: LiveData<Int> get() = _currentTabPosition

    private val _waterParameter = MutableLiveData<String>()
    val waterParameter: LiveData<String> get() = _waterParameter

    fun selectedTimeframesDate(selectedDate: Date) {
        _timeframesDate.value = selectedDate
    }
    fun selectedTabPosition(selectedTab: Int) {
        _currentTabPosition.value = selectedTab
    }

    fun selectedWaterParameter(selectedWaterParameter: String) {
        _waterParameter.value = selectedWaterParameter
    }
}