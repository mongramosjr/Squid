package com.squidsentry.mobile.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.squidsentry.mobile.ThingSpeak
import com.squidsentry.mobile.ThingSpeakApiClient
import com.squidsentry.mobile.data.model.WaterQuality
import com.squidsentry.mobile.data.model.WaterQualityTimeframe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
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

    // NOTE: Data that were structured based on the dynamics of TabLayout
    // current query based on chosen date per active parameter only
    private val _waterQualityTimeframe = MutableLiveData<WaterQualityTimeframe>()
    val waterQualityTimeframe: LiveData<WaterQualityTimeframe> get() = _waterQualityTimeframe

    // List of above data based on date keys
    private val _waterQualityTimeframeList =
        MutableLiveData<Map<String, MutableMap<LocalDate, WaterQualityTimeframe>>>().apply {
            mapOf("temperature" to null, "pH" to null,
            "salinity" to null, "dissolvedOxygen" to null,
            "tds" to null, "turbidity" to null)}
    val waterQualityTimeframeList: LiveData<Map<String,MutableMap<LocalDate, WaterQualityTimeframe>>> get() = _waterQualityTimeframeList


    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    private var errorMessage: String = ""

    // change to the selected date
    private val _isDone = MutableLiveData<Instant>()
    val isDone: LiveData<Instant> get() = _isDone

    // TODO: make this not a mutable livedata
    //  list of waterQualityTimeframe

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

                    // store
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

        val date: LocalDate = instantToLocalDate(selectedDate)
        var temperaturetimeframe: WaterQualityTimeframe?
        var phtimeframe: WaterQualityTimeframe?
        var salinitytimeframe: WaterQualityTimeframe?
        var dissolvedoxygentimeframe: WaterQualityTimeframe?
        var tdstimeframe: WaterQualityTimeframe?
        var turbiditytimeframe: WaterQualityTimeframe?

        /*
        temperaturetimeframe = _waterQualityTimeframeList.value?.get(date)
        if(waterqualitytimeframe==null){
            // store the request and response
            waterqualitytimeframe = WaterQualityTimeframe(selectedDate)
        }
*/
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
            tmp.dailyWaterQuality = WaterQuality(timeframe)
            tmp.dailyWaterQuality.measured = pH.toMutableList()
            tmp.dailyWaterQuality.datetime = pH_date.toMutableList()
            if(_pH.value!=null){
                _pH.value!!.dailyWaterQuality = tmp.dailyWaterQuality
            }else{
                _pH.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQuality = WaterQuality(timeframe)
            tmp.dailyWaterQuality.measured = salinity.toMutableList()
            tmp.dailyWaterQuality.datetime = salinity_date.toMutableList()
            if(_salinity.value!=null){
                _salinity.value!!.dailyWaterQuality = tmp.dailyWaterQuality
            }else{
                _salinity.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQuality = WaterQuality(timeframe)
            tmp.dailyWaterQuality.measured = dissolvedoxygen.toMutableList()
            tmp.dailyWaterQuality.datetime = dissolvedoxygen_date.toMutableList()
            if(_dissolvedOxygen.value!=null){
                _dissolvedOxygen.value!!.dailyWaterQuality = tmp.dailyWaterQuality
            }else{
                _dissolvedOxygen.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQuality = WaterQuality(timeframe)
            tmp.dailyWaterQuality.measured = tds.toMutableList()
            tmp.dailyWaterQuality.datetime = tds_date.toMutableList()
            if(_tds.value!=null){
                _tds.value!!.dailyWaterQuality = tmp.dailyWaterQuality
            }else{
                _tds.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQuality = WaterQuality(timeframe)
            tmp.dailyWaterQuality.measured = temperature.toMutableList()
            tmp.dailyWaterQuality.datetime = temperature_date.toMutableList()
            if(_temperature.value!=null){
                _temperature.value!!.dailyWaterQuality = tmp.dailyWaterQuality
            }else{
                _temperature.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.dailyWaterQuality = WaterQuality(timeframe)
            tmp.dailyWaterQuality.measured = turbidity.toMutableList()
            tmp.dailyWaterQuality.datetime = turbidity_date.toMutableList()
            if(_turbidity.value!=null){
                _turbidity.value!!.dailyWaterQuality = tmp.dailyWaterQuality
            }else{
                _turbidity.value = tmp
            }
        }

        if(timeframe==WEEKLY_TIMEFRAME){
            var tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQuality = WaterQuality(timeframe)
            tmp.weeklyWaterQuality.measured = pH.toMutableList()
            tmp.weeklyWaterQuality.datetime = pH_date.toMutableList()
            if(_pH.value!=null){
                _pH.value!!.weeklyWaterQuality = tmp.weeklyWaterQuality
            }else{
                _pH.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQuality = WaterQuality(timeframe)
            tmp.weeklyWaterQuality.measured = salinity.toMutableList()
            tmp.weeklyWaterQuality.datetime = salinity_date.toMutableList()
            if(_salinity.value!=null){
                _salinity.value!!.weeklyWaterQuality = tmp.weeklyWaterQuality
            }else{
                _salinity.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQuality = WaterQuality(timeframe)
            tmp.weeklyWaterQuality.measured = dissolvedoxygen.toMutableList()
            tmp.weeklyWaterQuality.datetime = dissolvedoxygen_date.toMutableList()
            if(_dissolvedOxygen.value!=null){
                _dissolvedOxygen.value!!.weeklyWaterQuality = tmp.weeklyWaterQuality
            }else{
                _dissolvedOxygen.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQuality = WaterQuality(timeframe)
            tmp.weeklyWaterQuality.measured = tds.toMutableList()
            tmp.weeklyWaterQuality.datetime = tds_date.toMutableList()
            if(_tds.value!=null){
                _tds.value!!.weeklyWaterQuality = tmp.weeklyWaterQuality
            }else{
                _tds.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQuality = WaterQuality(timeframe)
            tmp.weeklyWaterQuality.measured = temperature.toMutableList()
            tmp.weeklyWaterQuality.datetime = temperature_date.toMutableList()
            if(_temperature.value!=null){
                _temperature.value!!.weeklyWaterQuality = tmp.weeklyWaterQuality
            }else{
                _temperature.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.weeklyWaterQuality = WaterQuality(timeframe)
            tmp.weeklyWaterQuality.measured = turbidity.toMutableList()
            tmp.weeklyWaterQuality.datetime = turbidity_date.toMutableList()
            if(_turbidity.value!=null){
                _turbidity.value!!.weeklyWaterQuality = tmp.weeklyWaterQuality
            }else{
                _turbidity.value = tmp
            }
        }
        if(timeframe==MONTHLY_TIMEFRAME){
            var tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQuality = WaterQuality(timeframe)
            tmp.monthlyWaterQuality.measured = pH.toMutableList()
            tmp.monthlyWaterQuality.datetime = pH_date.toMutableList()
            if(_pH.value!=null){
                _pH.value!!.monthlyWaterQuality = tmp.monthlyWaterQuality
            }else{
                _pH.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQuality = WaterQuality(timeframe)
            tmp.monthlyWaterQuality.measured = salinity.toMutableList()
            tmp.monthlyWaterQuality.datetime = salinity_date.toMutableList()
            if(_salinity.value!=null){
                _salinity.value!!.monthlyWaterQuality = tmp.monthlyWaterQuality
            }else{
                _salinity.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQuality = WaterQuality(timeframe)
            tmp.monthlyWaterQuality.measured = dissolvedoxygen.toMutableList()
            tmp.monthlyWaterQuality.datetime = dissolvedoxygen_date.toMutableList()
            if(_dissolvedOxygen.value!=null){
                _dissolvedOxygen.value!!.monthlyWaterQuality = tmp.monthlyWaterQuality
            }else{
                _dissolvedOxygen.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQuality = WaterQuality(timeframe)
            tmp.monthlyWaterQuality.measured = tds.toMutableList()
            tmp.monthlyWaterQuality.datetime = tds_date.toMutableList()
            if(_tds.value!=null){
                _tds.value!!.monthlyWaterQuality = tmp.monthlyWaterQuality
            }else{
                _tds.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQuality = WaterQuality(timeframe)
            tmp.monthlyWaterQuality.measured = temperature.toMutableList()
            tmp.monthlyWaterQuality.datetime = temperature_date.toMutableList()
            if(_temperature.value!=null){
                _temperature.value!!.monthlyWaterQuality = tmp.monthlyWaterQuality
            }else{
                _temperature.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.monthlyWaterQuality = WaterQuality(timeframe)
            tmp.monthlyWaterQuality.measured = turbidity.toMutableList()
            tmp.monthlyWaterQuality.datetime = turbidity_date.toMutableList()
            if(_turbidity.value!=null){
                _turbidity.value!!.monthlyWaterQuality = tmp.monthlyWaterQuality
            }else{
                _turbidity.value = tmp
            }
        }

        if(timeframe==YEARLY_TIMEFRAME){
            var tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQuality = WaterQuality(timeframe)
            tmp.yearlyWaterQuality.measured = pH.toMutableList()
            tmp.yearlyWaterQuality.datetime = pH_date.toMutableList()
            if(_pH.value!=null){
                _pH.value!!.yearlyWaterQuality = tmp.yearlyWaterQuality
            }else{
                _pH.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQuality = WaterQuality(timeframe)
            tmp.yearlyWaterQuality.measured = salinity.toMutableList()
            tmp.yearlyWaterQuality.datetime = salinity_date.toMutableList()
            if(_salinity.value!=null){
                _salinity.value!!.yearlyWaterQuality = tmp.yearlyWaterQuality
            }else{
                _salinity.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQuality = WaterQuality(timeframe)
            tmp.yearlyWaterQuality.measured = dissolvedoxygen.toMutableList()
            tmp.yearlyWaterQuality.datetime = dissolvedoxygen_date.toMutableList()
            if(_dissolvedOxygen.value!=null){
                _dissolvedOxygen.value!!.yearlyWaterQuality = tmp.yearlyWaterQuality
            }else{
                _dissolvedOxygen.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQuality = WaterQuality(timeframe)
            tmp.yearlyWaterQuality.measured = tds.toMutableList()
            tmp.yearlyWaterQuality.datetime = tds_date.toMutableList()
            if(_tds.value!=null){
                _tds.value!!.yearlyWaterQuality = tmp.yearlyWaterQuality
            }else{
                _tds.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQuality = WaterQuality(timeframe)
            tmp.yearlyWaterQuality.measured = temperature.toMutableList()
            tmp.yearlyWaterQuality.datetime = temperature_date.toMutableList()
            if(_temperature.value!=null){
                _temperature.value!!.yearlyWaterQuality = tmp.yearlyWaterQuality
            }else{
                _temperature.value = tmp
            }
            tmp = WaterQualityTimeframe(selectedDate)
            tmp.yearlyWaterQuality = WaterQuality(timeframe)
            tmp.yearlyWaterQuality.measured = turbidity.toMutableList()
            tmp.yearlyWaterQuality.datetime = turbidity_date.toMutableList()
            if(_turbidity.value!=null){
                _turbidity.value!!.yearlyWaterQuality = tmp.yearlyWaterQuality
            }else{
                _turbidity.value = tmp
            }
        }

        //waterqualitytimeframe.dailyWaterQuality =
    }

    // TODO: return the data from the current request
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

    fun instantToLocalDateString(selectedDate: Instant = Instant.now(),
                                 pattern: String = "yyyy-MM-dd"): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
            .withZone(ZoneId.systemDefault())
        var dateformatted: String = "2023-10-20"
        dateformatted = formatter.format(selectedDate)
        return dateformatted
    }

    fun instantToLocalDate(selectedDate: Instant = Instant.now()): LocalDate{
        return selectedDate.atZone(ZoneId.systemDefault()).toLocalDate()
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
