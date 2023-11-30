package com.squidsentry.mobile.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.squidsentry.mobile.ThingSpeak
import com.squidsentry.mobile.ThingSpeakApiClient
import com.squidsentry.mobile.data.model.WaterQualityData
import com.squidsentry.mobile.data.model.WaterQualityTimeframe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

const val DAILY_TIMEFRAME: Int = 0
const val WEEKLY_TIMEFRAME: Int = 1
const val MONTHLY_TIMEFRAME: Int = 2
const val YEARLY_TIMEFRAME: Int = 3


class ThingSpeakViewModel : ViewModel() {

    // Response body from thingspeak
    private val _thingSpeakData = MutableLiveData<ThingSpeak?>()
    val thingSpeakData: LiveData<ThingSpeak?> get() = _thingSpeakData

    // Data that were structured based on the dyanmics of TabLayout
    // Only one parameter can hold
    private val _waterQualityTimeframe = MutableLiveData<WaterQualityTimeframe?>()
    val waterQualityTimeframe: LiveData<WaterQualityTimeframe?> get() = _waterQualityTimeframe

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    private var errorMessage: String = ""

    // change to the selected date
    private val _isDone = MutableLiveData<Instant>()
    val isDone: LiveData<Instant> get() = _isDone

    // list of waterQualityTimeframe
    private val _pH = MutableLiveData<WaterQualityTimeframe>()
    val pH: LiveData<WaterQualityTimeframe> get() = _pH

    private val _dissolvedOxygen = MutableLiveData<WaterQualityTimeframe>()
    val dissolvedOxygen: LiveData<WaterQualityTimeframe> get() = _dissolvedOxygen

    private val _salinity = MutableLiveData<WaterQualityTimeframe>()
    val salinity: LiveData<WaterQualityTimeframe> get() = _salinity

    private val _tds = MutableLiveData<WaterQualityTimeframe>()
    val tds: LiveData<WaterQualityTimeframe> get() = _tds

    private val _temperature = MutableLiveData<WaterQualityTimeframe>()
    val temperature: LiveData<WaterQualityTimeframe> get() = _temperature

    private val _turbidity = MutableLiveData<WaterQualityTimeframe>()
    val turbidity: LiveData<WaterQualityTimeframe> get() = _turbidity

    fun getLastWaterQuality(){

        _isLoading.value = true
        _isError.value = false

        val client = ThingSpeakApiClient.apiService.getLast(288)

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
                    _thingSpeakData.value = responseBody
                    Log.i("ViewModelHHHHHHHH", "Processing Response from ThingSpeak")
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

    fun rearrangeData(responseBody: ThingSpeak,
                      selectedDate: Instant = Instant.now(),
                      timeframe: Int = 0){

        //val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        //2023-04-06T00:13:00Z
        //val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")
        //date_last_turbidity = OffsetDateTime.parse(e.createdAt).toLocalDateTime()

        val feeds = responseBody.feeds?.listIterator()

        var pH = mutableListOf <FloatEntry>().apply {  }
        var temperature = mutableListOf <FloatEntry>().apply {  }
        var salinity = mutableListOf <FloatEntry>().apply {  }
        var dissolvedoxygen = mutableListOf <FloatEntry>().apply {  }
        var tds = mutableListOf <FloatEntry>().apply {  }
        var turbidity = mutableListOf <FloatEntry>().apply {  }

        var pH_date = mutableListOf <Instant>().apply {  }
        var temperature_date = mutableListOf <Instant>().apply {  }
        var salinity_date = mutableListOf <Instant>().apply {  }
        var dissolvedoxygen_date = mutableListOf <Instant>().apply {  }
        var tds_date = mutableListOf <Instant>().apply {  }
        var turbidity_date = mutableListOf <Instant>().apply {  }

        if(feeds!=null) {

            var idx_ph: Int = 0
            var idx_temperature: Int = 0
            var idx_salinity: Int = 0
            var idx_dissolvedoxygen: Int = 0
            var idx_tds: Int = 0
            var idx_turbidity: Int = 0

            while (feeds.hasNext()) {
                val e = feeds.next()

                val item_date = Instant.parse(e.createdAt)

                if(e.field1!=null) {
                    pH.add(idx_ph, FloatEntry(idx_ph.toFloat(), e.field1.toFloat()))
                    pH_date.add(item_date)
                    idx_ph++
                }
                if(e.field2!=null) {
                    temperature.add(idx_temperature, FloatEntry(idx_temperature.toFloat(), e.field2.toFloat()))
                    temperature_date.add(item_date)
                    idx_temperature++
                }
                if(e.field3!=null) {
                    salinity.add(idx_salinity, FloatEntry(idx_salinity.toFloat(), e.field3.toFloat()))
                    salinity_date.add(item_date)
                    idx_salinity++
                }
                if(e.field4!=null) {
                    dissolvedoxygen.add(idx_dissolvedoxygen, FloatEntry(idx_dissolvedoxygen.toFloat(), e.field4.toFloat()))
                    dissolvedoxygen_date.add(item_date)
                    idx_dissolvedoxygen++
                }
                if(e.field5!=null) {
                    tds.add(idx_tds, FloatEntry(idx_tds.toFloat(), e.field5.toFloat()))
                    tds_date.add(item_date)
                    idx_tds++
                }
                if(e.field6!=null) {
                    turbidity.add(idx_turbidity, FloatEntry(idx_turbidity.toFloat(), e.field6.toFloat()))
                    turbidity_date.add(item_date)
                    idx_turbidity++
                }
            }
        }

        // daily, weekly, monthly and yearly
        if(timeframe==DAILY_TIMEFRAME){
            var tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQualityData = WaterQualityData(timeframe)
            tmp.dailyWaterQualityData.measured = pH.toMutableList()
            tmp.dailyWaterQualityData.datetime = pH_date.toMutableList()
            if(_pH.value!=null){
                _pH.value!!.dailyWaterQualityData = tmp.dailyWaterQualityData
            }else{
                _pH.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQualityData = WaterQualityData(timeframe)
            tmp.dailyWaterQualityData.measured = salinity.toMutableList()
            tmp.dailyWaterQualityData.datetime = salinity_date.toMutableList()
            if(_salinity.value!=null){
                _salinity.value!!.dailyWaterQualityData = tmp.dailyWaterQualityData
            }else{
                _salinity.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQualityData = WaterQualityData(timeframe)
            tmp.dailyWaterQualityData.measured = dissolvedoxygen.toMutableList()
            tmp.dailyWaterQualityData.datetime = dissolvedoxygen_date.toMutableList()
            if(_dissolvedOxygen.value!=null){
                _dissolvedOxygen.value!!.dailyWaterQualityData = tmp.dailyWaterQualityData
            }else{
                _dissolvedOxygen.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQualityData = WaterQualityData(timeframe)
            tmp.dailyWaterQualityData.measured = tds.toMutableList()
            tmp.dailyWaterQualityData.datetime = tds_date.toMutableList()
            if(_tds.value!=null){
                _tds.value!!.dailyWaterQualityData = tmp.dailyWaterQualityData
            }else{
                _tds.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQualityData = WaterQualityData(timeframe)
            tmp.dailyWaterQualityData.measured = temperature.toMutableList()
            tmp.dailyWaterQualityData.datetime = temperature_date.toMutableList()
            if(_temperature.value!=null){
                _temperature.value!!.dailyWaterQualityData = tmp.dailyWaterQualityData
            }else{
                _temperature.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQualityData = WaterQualityData(timeframe)
            tmp.dailyWaterQualityData.measured = turbidity.toMutableList()
            tmp.dailyWaterQualityData.datetime = turbidity_date.toMutableList()
            if(_turbidity.value!=null){
                _turbidity.value!!.dailyWaterQualityData = tmp.dailyWaterQualityData
            }else{
                _turbidity.value = tmp
            }
        }

        if(timeframe==WEEKLY_TIMEFRAME){
            var tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQualityData = WaterQualityData(timeframe)
            tmp.weeklyWaterQualityData.measured = pH.toMutableList()
            tmp.weeklyWaterQualityData.datetime = pH_date.toMutableList()
            if(_pH.value!=null){
                _pH.value!!.weeklyWaterQualityData = tmp.weeklyWaterQualityData
            }else{
                _pH.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQualityData = WaterQualityData(timeframe)
            tmp.weeklyWaterQualityData.measured = salinity.toMutableList()
            tmp.weeklyWaterQualityData.datetime = salinity_date.toMutableList()
            if(_salinity.value!=null){
                _salinity.value!!.weeklyWaterQualityData = tmp.weeklyWaterQualityData
            }else{
                _salinity.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQualityData = WaterQualityData(timeframe)
            tmp.weeklyWaterQualityData.measured = dissolvedoxygen.toMutableList()
            tmp.weeklyWaterQualityData.datetime = dissolvedoxygen_date.toMutableList()
            if(_dissolvedOxygen.value!=null){
                _dissolvedOxygen.value!!.weeklyWaterQualityData = tmp.weeklyWaterQualityData
            }else{
                _dissolvedOxygen.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQualityData = WaterQualityData(timeframe)
            tmp.weeklyWaterQualityData.measured = tds.toMutableList()
            tmp.weeklyWaterQualityData.datetime = tds_date.toMutableList()
            if(_tds.value!=null){
                _tds.value!!.weeklyWaterQualityData = tmp.weeklyWaterQualityData
            }else{
                _tds.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQualityData = WaterQualityData(timeframe)
            tmp.weeklyWaterQualityData.measured = temperature.toMutableList()
            tmp.weeklyWaterQualityData.datetime = temperature_date.toMutableList()
            if(_temperature.value!=null){
                _temperature.value!!.weeklyWaterQualityData = tmp.weeklyWaterQualityData
            }else{
                _temperature.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQualityData = WaterQualityData(timeframe)
            tmp.weeklyWaterQualityData.measured = turbidity.toMutableList()
            tmp.weeklyWaterQualityData.datetime = turbidity_date.toMutableList()
            if(_turbidity.value!=null){
                _turbidity.value!!.weeklyWaterQualityData = tmp.weeklyWaterQualityData
            }else{
                _turbidity.value = tmp
            }
        }
        if(timeframe==MONTHLY_TIMEFRAME){
            var tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQualityData = WaterQualityData(timeframe)
            tmp.monthlyWaterQualityData.measured = pH.toMutableList()
            tmp.monthlyWaterQualityData.datetime = pH_date.toMutableList()
            if(_pH.value!=null){
                _pH.value!!.monthlyWaterQualityData = tmp.monthlyWaterQualityData
            }else{
                _pH.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQualityData = WaterQualityData(timeframe)
            tmp.monthlyWaterQualityData.measured = salinity.toMutableList()
            tmp.monthlyWaterQualityData.datetime = salinity_date.toMutableList()
            if(_salinity.value!=null){
                _salinity.value!!.monthlyWaterQualityData = tmp.monthlyWaterQualityData
            }else{
                _salinity.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQualityData = WaterQualityData(timeframe)
            tmp.monthlyWaterQualityData.measured = dissolvedoxygen.toMutableList()
            tmp.monthlyWaterQualityData.datetime = dissolvedoxygen_date.toMutableList()
            if(_dissolvedOxygen.value!=null){
                _dissolvedOxygen.value!!.monthlyWaterQualityData = tmp.monthlyWaterQualityData
            }else{
                _dissolvedOxygen.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQualityData = WaterQualityData(timeframe)
            tmp.monthlyWaterQualityData.measured = tds.toMutableList()
            tmp.monthlyWaterQualityData.datetime = tds_date.toMutableList()
            if(_tds.value!=null){
                _tds.value!!.monthlyWaterQualityData = tmp.monthlyWaterQualityData
            }else{
                _tds.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQualityData = WaterQualityData(timeframe)
            tmp.monthlyWaterQualityData.measured = temperature.toMutableList()
            tmp.monthlyWaterQualityData.datetime = temperature_date.toMutableList()
            if(_temperature.value!=null){
                _temperature.value!!.monthlyWaterQualityData = tmp.monthlyWaterQualityData
            }else{
                _temperature.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQualityData = WaterQualityData(timeframe)
            tmp.monthlyWaterQualityData.measured = turbidity.toMutableList()
            tmp.monthlyWaterQualityData.datetime = turbidity_date.toMutableList()
            if(_turbidity.value!=null){
                _turbidity.value!!.monthlyWaterQualityData = tmp.monthlyWaterQualityData
            }else{
                _turbidity.value = tmp
            }
        }

        if(timeframe==YEARLY_TIMEFRAME){
            var tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQualityData = WaterQualityData(timeframe)
            tmp.yearlyWaterQualityData.measured = pH.toMutableList()
            tmp.yearlyWaterQualityData.datetime = pH_date.toMutableList()
            if(_pH.value!=null){
                _pH.value!!.yearlyWaterQualityData = tmp.yearlyWaterQualityData
            }else{
                _pH.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQualityData = WaterQualityData(timeframe)
            tmp.yearlyWaterQualityData.measured = salinity.toMutableList()
            tmp.yearlyWaterQualityData.datetime = salinity_date.toMutableList()
            if(_salinity.value!=null){
                _salinity.value!!.yearlyWaterQualityData = tmp.yearlyWaterQualityData
            }else{
                _salinity.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQualityData = WaterQualityData(timeframe)
            tmp.yearlyWaterQualityData.measured = dissolvedoxygen.toMutableList()
            tmp.yearlyWaterQualityData.datetime = dissolvedoxygen_date.toMutableList()
            if(_dissolvedOxygen.value!=null){
                _dissolvedOxygen.value!!.yearlyWaterQualityData = tmp.yearlyWaterQualityData
            }else{
                _dissolvedOxygen.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQualityData = WaterQualityData(timeframe)
            tmp.yearlyWaterQualityData.measured = tds.toMutableList()
            tmp.yearlyWaterQualityData.datetime = tds_date.toMutableList()
            if(_tds.value!=null){
                _tds.value!!.yearlyWaterQualityData = tmp.yearlyWaterQualityData
            }else{
                _tds.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQualityData = WaterQualityData(timeframe)
            tmp.yearlyWaterQualityData.measured = temperature.toMutableList()
            tmp.yearlyWaterQualityData.datetime = temperature_date.toMutableList()
            if(_temperature.value!=null){
                _temperature.value!!.yearlyWaterQualityData = tmp.yearlyWaterQualityData
            }else{
                _temperature.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQualityData = WaterQualityData(timeframe)
            tmp.yearlyWaterQualityData.measured = turbidity.toMutableList()
            tmp.yearlyWaterQualityData.datetime = turbidity_date.toMutableList()
            if(_turbidity.value!=null){
                _turbidity.value!!.yearlyWaterQualityData = tmp.yearlyWaterQualityData
            }else{
                _turbidity.value = tmp
            }
        }
    }

    fun getSelectedWaterQualityData(waterParameter: String): WaterQualityTimeframe? {

        var tmp: WaterQualityTimeframe? = null
        if(waterParameter=="pH"){
            tmp =  _pH.value
        }
        if(waterParameter=="temperature"){
            tmp =  _temperature.value
        }
        if(waterParameter=="dissolvedOxygen"){
            tmp =  _dissolvedOxygen.value
        }
        if(waterParameter=="salinity"){
            tmp =  _salinity.value
        }
        if(waterParameter=="tds"){
            tmp =  _tds.value
        }
        if(waterParameter=="turbidity"){
            tmp =  _turbidity.value
        }
        return tmp
    }

    //@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun getWaterQuality(selectedDate: Instant = Instant.now(),
                        timeframe: Int = 0){
        _isLoading.value = true
        _isError.value = false

        //compute the start and end
        val (start, end) = computeTimeframe(selectedDate, timeframe)

        var average: Int = 0
        //daily average = 0 = 288 results
        //weekly average = 4 = 504 results
        //monthly average = 12 = 720 results
        //yearly average = 120 = 876 results
        average = if(timeframe==YEARLY_TIMEFRAME) {
            120
        } else if(timeframe==MONTHLY_TIMEFRAME){
            12
        }else if(timeframe==WEEKLY_TIMEFRAME){
            4
        }else{
            0
        }

        val client = ThingSpeakApiClient.apiService.getData(start, end, average)

        client.enqueue(object : Callback<ThingSpeak> {

            override fun onResponse(call: Call<ThingSpeak>,
                                    response: Response<ThingSpeak>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody == null) {
                        onError("getWaterQuality: Data Processing Error")
                        return
                    }
                    // save to the viewmodel
                    Log.i("ViewModelHHHHHHHH", "getWaterQuality Size of feeds" + responseBody.feeds?.size.toString())
                    rearrangeData(responseBody, selectedDate, timeframe)
                    Log.i("ViewModelHHHHHHHH", "getWaterQuality: Processing Response from ThingSpeak")
                    _isLoading.value = false
                    _isDone.value = selectedDate
                } else {
                    // Handle error
                    print("${response.errorBody()} ")
                    onError("getWaterQuality: Data Processing Error")
                    return
                }
            }

            override fun onFailure(call: Call<ThingSpeak>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }
        })


    }

    fun computeTimeframe(selectedDate: Instant = Instant.now(), timeframe: Int = 0):
            Pair<String, String> {

        var start: String = ""
        var end: String = ""

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault())
        val zone = ZoneId.of("Asia/Manila")

        if(timeframe==YEARLY_TIMEFRAME){
            //val date = LocalDate.ofInstant(selectedDate, zone)
            val date = selectedDate.atZone(zone).toLocalDate()
            //val sundayNext = date.plusDays((7 - date.getDayOfWeek().value).toLong())
            val firstdayofYear = date.with(TemporalAdjusters.firstDayOfYear())
            val lastdayofYear = date.with(TemporalAdjusters.lastDayOfYear())
            start = "$firstdayofYear 00:00:00"
            end = "$lastdayofYear 23:59:59"
            Log.i("TIMEHHHHHHHHH", "$start + $end")
        }else if(timeframe==WEEKLY_TIMEFRAME){
            val date = selectedDate.atZone(zone).toLocalDate()
            val saturday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
            val sunday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            start = "$sunday 00:00:00"
            end = "$saturday 23:59:59"
            Log.i("TIMEHHHHHHHHH", "$start + $end")
        }else if(timeframe==MONTHLY_TIMEFRAME){
            val date = selectedDate.atZone(zone).toLocalDate()
            val firstdayofMonth = date.with(TemporalAdjusters.firstDayOfMonth())
            val lastdayofMonth = date.with(TemporalAdjusters.lastDayOfMonth())
            start = "$firstdayofMonth 00:00:00"
            end = "$lastdayofMonth 23:59:59"
            Log.i("TIMEHHHHHHHHH", "$start + $end")
        }else{
            start = formatter.format(selectedDate) + " 00:00:00"
            end = formatter.format(selectedDate) + " 23:59:59"
            Log.i("TIMEHHHHHHHHH", "$start + $end")
        }
        return Pair(start, end)
    }
}
