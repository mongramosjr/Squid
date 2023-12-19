package app.sthenoteuthis.mobile.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class TimeframeViewModel : ViewModel() {
    private val _timeframesDate = MutableLiveData<LocalDate>()
    val timeframesDate: LiveData<LocalDate> get() = _timeframesDate

    private val _currentTabPosition = MutableLiveData<Int>()
    val currentTabPosition: LiveData<Int> get() = _currentTabPosition

    private val _waterParameter = MutableLiveData<String>()
    val waterParameter: LiveData<String> get() = _waterParameter

    private val _waterParameterUom = MutableLiveData<String>()
    val waterParameterUom: LiveData<String> get() = _waterParameterUom

    fun selectedTimeframesDate(selectedDate: LocalDate) {
        _timeframesDate.value = selectedDate
    }
    fun selectedTabPosition(selectedTab: Int) {
        _currentTabPosition.value = selectedTab
    }

    fun selectedWaterParameter(selectedWaterParameter: String) {
        _waterParameter.value = selectedWaterParameter
    }
    fun selectedWaterParameterUom(selectedWaterParameterUom: String) {
        _waterParameterUom.value = selectedWaterParameterUom
    }

}