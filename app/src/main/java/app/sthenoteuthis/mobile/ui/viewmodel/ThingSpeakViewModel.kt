package app.sthenoteuthis.mobile.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.sthenoteuthis.mobile.SquidUtils
import com.patrykandpatrick.vico.core.entry.FloatEntry
import app.sthenoteuthis.mobile.data.model.ThingSpeak
import app.sthenoteuthis.mobile.data.ThingSpeakRepository
import app.sthenoteuthis.mobile.data.model.Feed
import app.sthenoteuthis.mobile.data.model.ThingSpeakDailyAverage
import app.sthenoteuthis.mobile.data.model.ThingSpeakDailyResult
import app.sthenoteuthis.mobile.data.model.ThingSpeakMonthlyAverage
import app.sthenoteuthis.mobile.data.model.ThingSpeakWeeklyAverage
import app.sthenoteuthis.mobile.data.model.ThingSpeakYearlyAverage
import app.sthenoteuthis.mobile.data.model.WaterQualityData
import app.sthenoteuthis.mobile.data.model.WaterQualityTimeframe
import app.sthenoteuthis.mobile.data.model.toFeed
import app.sthenoteuthis.mobile.ui.turbidity.TurbidityFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

const val DAILY_TIMEFRAME: Int = 0
const val WEEKLY_TIMEFRAME: Int = 1
const val MONTHLY_TIMEFRAME: Int = 2
const val YEARLY_TIMEFRAME: Int = 3

const val PH: String = "pH"
const val TEMPERATURE: String = "Temperature"
const val DISSOLVED_OXYGEN: String = "Dissolved Oxygen"
const val SALINITY: String = "Salinity"
const val TDS: String = "TDS"
const val TURBIDITY: String = "Turbidity"


class ThingSpeakViewModel(private val repository: ThingSpeakRepository) : ViewModel() {

    // Response from thingspeak
    private val _thingSpeakData = MutableLiveData<List<Feed>>()
    val thingSpeakData: LiveData<List<Feed>> get() = _thingSpeakData

    private val _lastDateEntry = MutableLiveData<Instant>()
    val lastDateEntry: LiveData<Instant> = _lastDateEntry

    private val _lastDateEntryCount = MutableLiveData<Int>()
    val lastDateEntryCount: LiveData<Int> = _lastDateEntryCount

    // List of water quality feeds from remote
    // based on the hierarchical structure of water parameters, dates and timeframe
    // date-timeframe is one-to-many relationship
    // parameters-date is one-to-many relationship
    private val _waterQualityData =
        MutableLiveData<WaterQualityData>()
    val waterQualityData: LiveData<WaterQualityData> get() = _waterQualityData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    private var errorMessage: String = ""

    // store all that the dates queried
    // if date is already in the list, do not fetch feeds from remote
    private val _isDone = MutableLiveData<RequestDone>()
    val isDone: LiveData<RequestDone> get() = _isDone


    companion object {
        private const val TAG = "ThingSpeakViewModel"
    }

    fun defaultValues()
    {
        _waterQualityData.value = WaterQualityData()
        _lastDateEntryCount.value = 0
    }

    suspend fun countFeedsLocal(): Int{
        return repository.size()
    }

    fun fetchLastWaterQuality(entries: Int = ThingSpeakDailyResult){

        _isLoading.value = true
        _isError.value = false

        //val client = ThingSpeakApiClient.apiService.getLast(entries)
        val client = repository.fetchLastFeeds(entries)
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

                    if (responseBody.feeds != null) {
                        val feeds: List<Feed> = responseBody.feeds
                        // broadcast these three variables
                        _thingSpeakData.value = feeds
                        _lastDateEntry.value = pickDateLastEntry(feeds)
                        _lastDateEntryCount.value = 1 // NOTE: always reset to 1
                    }

                    // TODO: store responseBody to local database FeedsEntity
                    Log.d(TAG, "getLastWaterQuality: Processing Response from ThingSpeak ")
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


    private fun fetchWaterQualityAt(selectedDate: Instant = Instant.now(),
                                    timeframe: Int = 0){

        Log.d(TAG, "fetchWaterQualityAt: Processing.. $this")


        _isLoading.value = true
        _isError.value = false

        //compute the start and end
        val (start, end) = SquidUtils.computeTimeframe(selectedDate, timeframe)

        var average: Int = 0
        //daily average = 0 = 288 results
        //weekly average = 4 = 504 results
        //monthly average = 12 = 720 results
        //yearly average = 120 = 876 results
        average = if(timeframe== YEARLY_TIMEFRAME) {
            ThingSpeakYearlyAverage
        } else if(timeframe== MONTHLY_TIMEFRAME){
            ThingSpeakMonthlyAverage
        }else if(timeframe== WEEKLY_TIMEFRAME){
            ThingSpeakWeeklyAverage
        }else{
            ThingSpeakDailyAverage
        }

        var check_dates: MutableList<Pair<Int, LocalDate>> = mutableListOf<Pair<Int,LocalDate>>().apply{}

        if(_isDone.isInitialized) {
            check_dates = _isDone.value!!.dates
            Log.d(TAG, "fetchWaterQualityAt: COUNT is " + check_dates.size.toString())
            val date_timeframe: Pair<Int, LocalDate> = (timeframe to instantToLocalDate(selectedDate))
            if(check_dates.contains(date_timeframe)){
                Log.d(TAG, "fetchWaterQualityAt: ALREADY HAS " + instantToLocalDate(selectedDate).toString())
                _isDone.value = RequestDone(Instant.now(), selectedDate, check_dates)
                return
            }
        }else{
            Log.d(TAG, "fetchWaterQualityAt: COUNT is 0")
        }

        //val client = ThingSpeakApiClient.apiService.getData(start, end, average)
        val client = repository.fetchFeeds(start, end, average)

        client.enqueue(object : Callback<ThingSpeak> {

            override fun onResponse(call: Call<ThingSpeak>,
                                    response: Response<ThingSpeak>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody == null) {
                        onError("fetchWaterQualityAt: Data Processing Error")
                        return
                    }
                    // save to the viewmodel
                    Log.d(TAG, "fetchWaterQualityAt: Size of feeds: " + responseBody.feeds?.size.toString())
                    if (responseBody.feeds != null) {
                        val feeds: List<Feed> = responseBody.feeds

                        rearrangeData(feeds, selectedDate, timeframe)
                        setRequestDone(selectedDate, timeframe)

                    }

                    if (responseBody.feeds != null) {
                        // store responseBody to local database FeedsEntity
                        viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    repository.insertFeeds(responseBody)
                                } catch (e: Exception) {
                                    Log.e(TAG, e.toString() )
                                }
                            }
                        }
                    }

                } else {
                    // Handle error
                    print("${response.errorBody()} ")
                    onError("fetchWaterQualityAt: Data Processing Error")
                    return
                }
            }

            override fun onFailure(call: Call<ThingSpeak>, t: Throwable) {
                onError(t.message)
                t.printStackTrace()
            }
        })
    }


    fun fetchWaterQuality(selectedDate: Instant = Instant.now(),
                          timeframe: Int = DAILY_TIMEFRAME, isAll: Boolean = false)
    {
        if(isAll){
            fetchWaterQualityAt(selectedDate, DAILY_TIMEFRAME)
            fetchWaterQualityAt(selectedDate, WEEKLY_TIMEFRAME)
            fetchWaterQualityAt(selectedDate, MONTHLY_TIMEFRAME)
            fetchWaterQualityAt(selectedDate, YEARLY_TIMEFRAME)
        }else{
            fetchWaterQualityAt(selectedDate, timeframe)
        }
    }

    fun queryLastFeeds(){
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                val recentfeedentities = repository.findLastFeeds()
                val feedentities = recentfeedentities.listIterator()
                val feeds: MutableList<Feed> = mutableListOf()
                while (feedentities.hasNext()) {
                    val e = feedentities.next()
                    feeds.add(e.toFeed())
                }
                if(recentfeedentities.isNotEmpty()){
                    val lastfeedentity = recentfeedentities.last()
                    val dateLastEntry = lastfeedentity.createdAt
                    setThingSpeakData(feeds.toList())
                    setLastDateEntry(dateLastEntry)
                    setLastDateEntryCount(1)
                }
            }
        }
    }

    fun queryFeedsAt(selectedDate: Instant = Instant.now(), timeframe: Int = DAILY_TIMEFRAME){

        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                val (start, end) = SquidUtils.computeTimeframe(selectedDate, timeframe)
                val since = SquidUtils.dateTimeStringToMilliseconds(start)
                val until = SquidUtils.dateTimeStringToMilliseconds(end)
                val recentfeedentities = repository.findByDateRange(since, until)
                val feedentities = recentfeedentities.listIterator()
                val feeds: MutableList<Feed> = mutableListOf()
                val size = recentfeedentities.size
                while (feedentities.hasNext()) {
                    val e = feedentities.next()
                    feeds.add(e.toFeed())
                }
                // save to mutablelivedata
                rearrangeData(feeds.toList(), selectedDate, timeframe)
                setRequestDone(selectedDate, timeframe)
            }
        }
    }

    fun queryFeeds(selectedDate: Instant = Instant.now(),
                        timeframe: Int = DAILY_TIMEFRAME, isAll: Boolean = false)
    {
        if(isAll){
            queryFeedsAt(selectedDate, DAILY_TIMEFRAME)
            queryFeedsAt(selectedDate, WEEKLY_TIMEFRAME)
            queryFeedsAt(selectedDate, MONTHLY_TIMEFRAME)
            queryFeedsAt(selectedDate, YEARLY_TIMEFRAME)
        }else{
            queryFeedsAt(selectedDate, timeframe)
        }
    }

    private fun onError(inputMessage: String?) {

        val message = if (inputMessage.isNullOrBlank() or inputMessage.isNullOrEmpty()) "Unknown Error"
        else inputMessage

        errorMessage = StringBuilder("ERROR: ")
            .append("$message some data may not displayed properly").toString()

        _isError.value = true
        _isLoading.value = false
    }

    fun rearrangeData(responseBody: List<Feed>,
                      selectedDate: Instant = Instant.now(),
                      timeframe: Int = DAILY_TIMEFRAME){

        //val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        //2023-04-06T00:13:00Z
        //val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")
        //date_last_turbidity = OffsetDateTime.parse(e.createdAt).toLocalDateTime()

        val date: LocalDate = instantToLocalDate(selectedDate)

        // copy from mutable live data
        // TODO: not sure if this is recommended
        var temperatureData: MutableMap<LocalDate, WaterQualityTimeframe>? = _waterQualityData.value?.temperature
        var pHData: MutableMap<LocalDate, WaterQualityTimeframe>? = _waterQualityData.value?.pH
        var dissolvedOxygenData: MutableMap<LocalDate, WaterQualityTimeframe>? = _waterQualityData.value?.dissolvedOxygen
        var salinityData: MutableMap<LocalDate, WaterQualityTimeframe>? = _waterQualityData.value?.salinity
        var tdsData: MutableMap<LocalDate, WaterQualityTimeframe>? = _waterQualityData.value?.tds
        var turbidityData: MutableMap<LocalDate, WaterQualityTimeframe>? = _waterQualityData.value?.turbidity

        val feeds = responseBody.listIterator()

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

                if(e.pH!=null) {
                    pH.add(idx_ph, FloatEntry(idx_ph.toFloat(), e.pH!!.toFloat()))
                    pH_date.add(item_date)
                    idx_ph++
                }
                if(e.temperature!=null) {
                    temperature.add(idx_temperature, FloatEntry(idx_temperature.toFloat(), e.temperature!!.toFloat()))
                    temperature_date.add(item_date)
                    idx_temperature++
                }
                if(e.salinity!=null) {
                    salinity.add(idx_salinity, FloatEntry(idx_salinity.toFloat(), e.salinity!!.toFloat()))
                    salinity_date.add(item_date)
                    idx_salinity++
                }
                if(e.dissolvedOxygen!=null) {
                    dissolvedoxygen.add(idx_dissolvedoxygen, FloatEntry(idx_dissolvedoxygen.toFloat(), e.dissolvedOxygen!!.toFloat()))
                    dissolvedoxygen_date.add(item_date)
                    idx_dissolvedoxygen++
                }
                if(e.tds!=null) {
                    tds.add(idx_tds, FloatEntry(idx_tds.toFloat(), e.tds!!.toFloat()))
                    tds_date.add(item_date)
                    idx_tds++
                }
                if(e.turbidity!=null) {
                    turbidity.add(idx_turbidity, FloatEntry(idx_turbidity.toFloat(), e.turbidity!!.toFloat()))
                    turbidity_date.add(item_date)
                    idx_turbidity++
                }
            }
        }

        // daily, weekly, monthly and yearly
        if(timeframe== DAILY_TIMEFRAME){
            var size: Int?
            size = 0
            // if data on the specified date does not exist, create one
            if(temperatureData?.contains(date)==false){
                temperatureData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = temperatureData?.get(date)?.dailyWaterQuality?.measured?.size
            if(size==0){
                temperatureData?.get(date)?.updatedAt = selectedDate
                temperatureData?.get(date)?.dailyWaterQuality?.measured = temperature.toMutableList()
                temperatureData?.get(date)?.dailyWaterQuality?.datetime = temperature_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(pHData?.contains(date)==false){
                pHData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = pHData?.get(date)?.dailyWaterQuality?.measured?.size
            if(size==0){
                pHData?.get(date)?.updatedAt = selectedDate
                pHData?.get(date)?.dailyWaterQuality?.measured = pH.toMutableList()
                pHData?.get(date)?.dailyWaterQuality?.datetime = pH_date.toMutableList()
            }

            // if data on the specified date does not exist, create one
            if(salinityData?.contains(date)==false){
                salinityData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = salinityData?.get(date)?.dailyWaterQuality?.measured?.size
            if(size==0){
                salinityData?.get(date)?.updatedAt = selectedDate
                salinityData?.get(date)?.dailyWaterQuality?.measured = salinity.toMutableList()
                salinityData?.get(date)?.dailyWaterQuality?.datetime = salinity_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(dissolvedOxygenData?.contains(date)==false){
                dissolvedOxygenData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = dissolvedOxygenData?.get(date)?.dailyWaterQuality?.measured?.size
            if(size==0){
                dissolvedOxygenData?.get(date)?.updatedAt = selectedDate
                dissolvedOxygenData?.get(date)?.dailyWaterQuality?.measured = dissolvedoxygen.toMutableList()
                dissolvedOxygenData?.get(date)?.dailyWaterQuality?.datetime = dissolvedoxygen_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(tdsData?.contains(date)==false){
                tdsData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = tdsData?.get(date)?.dailyWaterQuality?.measured?.size
            if(size==0){
                tdsData?.get(date)?.updatedAt = selectedDate
                tdsData?.get(date)?.dailyWaterQuality?.measured = tds.toMutableList()
                tdsData?.get(date)?.dailyWaterQuality?.datetime = tds_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(turbidityData?.contains(date)==false){
                turbidityData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = turbidityData?.get(date)?.dailyWaterQuality?.measured?.size
            if(size==0){
                turbidityData?.get(date)?.updatedAt = selectedDate
                turbidityData?.get(date)?.dailyWaterQuality?.measured = turbidity.toMutableList()
                turbidityData?.get(date)?.dailyWaterQuality?.datetime = turbidity_date.toMutableList()
            }
        }

        if(timeframe== WEEKLY_TIMEFRAME){
            var size: Int?
            size = 0
            // if data on the specified date does not exist, create one
            if(temperatureData?.contains(date)==false){
                temperatureData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = temperatureData?.get(date)?.weeklyWaterQuality?.measured?.size
            if(size==0){
                temperatureData?.get(date)?.updatedAt = selectedDate
                temperatureData?.get(date)?.weeklyWaterQuality?.measured = temperature.toMutableList()
                temperatureData?.get(date)?.weeklyWaterQuality?.datetime = temperature_date.toMutableList()
            }

            // if data on the specified date does not exist, create one
            if(pHData?.contains(date)==false){
                pHData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = pHData?.get(date)?.weeklyWaterQuality?.measured?.size
            if(size==0){
                pHData?.get(date)?.updatedAt = selectedDate
                pHData?.get(date)?.weeklyWaterQuality?.measured = pH.toMutableList()
                pHData?.get(date)?.weeklyWaterQuality?.datetime = pH_date.toMutableList()
            }

            // if data on the specified date does not exist, create one
            if(salinityData?.contains(date)==false){
                salinityData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = salinityData?.get(date)?.weeklyWaterQuality?.measured?.size
            if(size==0){
                salinityData?.get(date)?.updatedAt = selectedDate
                salinityData?.get(date)?.weeklyWaterQuality?.measured = salinity.toMutableList()
                salinityData?.get(date)?.weeklyWaterQuality?.datetime = salinity_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(dissolvedOxygenData?.contains(date)==false){
                dissolvedOxygenData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = dissolvedOxygenData?.get(date)?.weeklyWaterQuality?.measured?.size
            if(size==0){
                dissolvedOxygenData?.get(date)?.updatedAt = selectedDate
                dissolvedOxygenData?.get(date)?.weeklyWaterQuality?.measured = dissolvedoxygen.toMutableList()
                dissolvedOxygenData?.get(date)?.weeklyWaterQuality?.datetime = dissolvedoxygen_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(tdsData?.contains(date)==false){
                tdsData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = tdsData?.get(date)?.weeklyWaterQuality?.measured?.size
            if(size==0){
                tdsData?.get(date)?.updatedAt = selectedDate
                tdsData?.get(date)?.weeklyWaterQuality?.measured = tds.toMutableList()
                tdsData?.get(date)?.weeklyWaterQuality?.datetime = tds_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(turbidityData?.contains(date)==false){
                turbidityData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = turbidityData?.get(date)?.weeklyWaterQuality?.measured?.size
            if(size==0){
                turbidityData?.get(date)?.updatedAt = selectedDate
                turbidityData?.get(date)?.weeklyWaterQuality?.measured = turbidity.toMutableList()
                turbidityData?.get(date)?.weeklyWaterQuality?.datetime = turbidity_date.toMutableList()
            }
        }
        if(timeframe== MONTHLY_TIMEFRAME){
            var size: Int?
            size = 0
            // if data on the specified date does not exist, create one
            if(temperatureData?.contains(date)==false){
                temperatureData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = temperatureData?.get(date)?.monthlyWaterQuality?.measured?.size
            if(size==0){
                temperatureData?.get(date)?.updatedAt = selectedDate
                temperatureData?.get(date)?.monthlyWaterQuality?.measured = temperature.toMutableList()
                temperatureData?.get(date)?.monthlyWaterQuality?.datetime = temperature_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(pHData?.contains(date)==false){
                pHData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = pHData?.get(date)?.monthlyWaterQuality?.measured?.size
            if(size==0){
                pHData?.get(date)?.updatedAt = selectedDate
                pHData?.get(date)?.monthlyWaterQuality?.measured = pH.toMutableList()
                pHData?.get(date)?.monthlyWaterQuality?.datetime = pH_date.toMutableList()
            }

            // if data on the specified date does not exist, create one
            if(salinityData?.contains(date)==false){
                salinityData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = salinityData?.get(date)?.monthlyWaterQuality?.measured?.size
            if(size==0){
                salinityData?.get(date)?.updatedAt = selectedDate
                salinityData?.get(date)?.monthlyWaterQuality?.measured = salinity.toMutableList()
                salinityData?.get(date)?.monthlyWaterQuality?.datetime = salinity_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(dissolvedOxygenData?.contains(date)==false){
                dissolvedOxygenData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = dissolvedOxygenData?.get(date)?.monthlyWaterQuality?.measured?.size
            if(size==0){
                dissolvedOxygenData?.get(date)?.updatedAt = selectedDate
                dissolvedOxygenData?.get(date)?.monthlyWaterQuality?.measured = dissolvedoxygen.toMutableList()
                dissolvedOxygenData?.get(date)?.monthlyWaterQuality?.datetime = dissolvedoxygen_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(tdsData?.contains(date)==false){
                tdsData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = tdsData?.get(date)?.monthlyWaterQuality?.measured?.size
            if(size==0){
                tdsData?.get(date)?.updatedAt = selectedDate
                tdsData?.get(date)?.monthlyWaterQuality?.measured = tds.toMutableList()
                tdsData?.get(date)?.monthlyWaterQuality?.datetime = tds_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(turbidityData?.contains(date)==false){
                turbidityData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = turbidityData?.get(date)?.monthlyWaterQuality?.measured?.size
            if(size==0){
                turbidityData?.get(date)?.updatedAt = selectedDate
                turbidityData?.get(date)?.monthlyWaterQuality?.measured = turbidity.toMutableList()
                turbidityData?.get(date)?.monthlyWaterQuality?.datetime = turbidity_date.toMutableList()
            }
        }

        if(timeframe== YEARLY_TIMEFRAME){
            var size: Int?
            size = 0
            // if data on the specified date does not exist, create one
            if(temperatureData?.contains(date)==false){
                temperatureData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = temperatureData?.get(date)?.yearlyWaterQuality?.measured?.size
            if(size==0){
                temperatureData?.get(date)?.updatedAt = selectedDate
                temperatureData?.get(date)?.yearlyWaterQuality?.measured = temperature.toMutableList()
                temperatureData?.get(date)?.yearlyWaterQuality?.datetime = temperature_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(pHData?.contains(date)==false){
                pHData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = pHData?.get(date)?.yearlyWaterQuality?.measured?.size
            if(size==0){
                pHData?.get(date)?.updatedAt = selectedDate
                pHData?.get(date)?.yearlyWaterQuality?.measured = pH.toMutableList()
                pHData?.get(date)?.yearlyWaterQuality?.datetime = pH_date.toMutableList()
            }

            // if data on the specified date does not exist, create one
            if(salinityData?.contains(date)==false){
                salinityData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = salinityData?.get(date)?.yearlyWaterQuality?.measured?.size
            if(size==0){
                salinityData?.get(date)?.updatedAt = selectedDate
                salinityData?.get(date)?.yearlyWaterQuality?.measured = salinity.toMutableList()
                salinityData?.get(date)?.yearlyWaterQuality?.datetime = salinity_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(dissolvedOxygenData?.contains(date)==false){
                dissolvedOxygenData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = dissolvedOxygenData?.get(date)?.yearlyWaterQuality?.measured?.size
            if(size==0){
                dissolvedOxygenData?.get(date)?.updatedAt = selectedDate
                dissolvedOxygenData?.get(date)?.yearlyWaterQuality?.measured = dissolvedoxygen.toMutableList()
                dissolvedOxygenData?.get(date)?.yearlyWaterQuality?.datetime = dissolvedoxygen_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(tdsData?.contains(date)==false){
                tdsData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = tdsData?.get(date)?.yearlyWaterQuality?.measured?.size
            if(size==0){
                tdsData?.get(date)?.updatedAt = selectedDate
                tdsData?.get(date)?.yearlyWaterQuality?.measured = tds.toMutableList()
                tdsData?.get(date)?.yearlyWaterQuality?.datetime = tds_date.toMutableList()
            }
            // if data on the specified date does not exist, create one
            if(turbidityData?.contains(date)==false){
                turbidityData[date] = WaterQualityTimeframe(selectedDate)
            }
            size = turbidityData?.get(date)?.yearlyWaterQuality?.measured?.size
            if(size==0){
                turbidityData?.get(date)?.updatedAt = selectedDate
                turbidityData?.get(date)?.yearlyWaterQuality?.measured = turbidity.toMutableList()
                turbidityData?.get(date)?.yearlyWaterQuality?.datetime = turbidity_date.toMutableList()
            }
        }

        //recopy to mutable live data
        if (temperatureData != null) {
            _waterQualityData.value?.temperature = temperatureData
        }
        if (pHData != null) {
            _waterQualityData.value?.pH = pHData
        }
        if (dissolvedOxygenData != null) {
            _waterQualityData.value?.dissolvedOxygen = dissolvedOxygenData
        }
        if (salinityData != null) {
            _waterQualityData.value?.salinity = salinityData
        }
        if (tdsData != null) {
            _waterQualityData.value?.tds = tdsData
        }
        if (turbidityData != null) {
            _waterQualityData.value?.turbidity = turbidityData
        }
    }

    // return the data from the current request
    fun getSelectedWaterQualityData(waterParameter: String, date: LocalDate): WaterQualityTimeframe? {

        var tmp: WaterQualityTimeframe? = null
        if(waterParameter== PH){
            tmp =  _waterQualityData.value?.pH?.get(date)
        }
        if(waterParameter== TEMPERATURE){
            tmp =  _waterQualityData.value?.temperature?.get(date)
        }
        if(waterParameter== DISSOLVED_OXYGEN){
            tmp =  _waterQualityData.value?.dissolvedOxygen?.get(date)
        }
        if(waterParameter== SALINITY){
            tmp =  _waterQualityData.value?.salinity?.get(date)
        }
        if(waterParameter== TDS){
            tmp =  _waterQualityData.value?.tds?.get(date)
        }
        if(waterParameter== TURBIDITY){
            tmp =  _waterQualityData.value?.turbidity?.get(date)
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

    private fun pickDateLastEntry(feeds: List<Feed>): Instant{
        var dateLastEntry: Instant = Instant.now()
        val size = feeds.size
        if(size > 0){
            val createdAt = feeds.last().createdAt
            dateLastEntry = OffsetDateTime.parse(createdAt).toInstant()
        }
        return dateLastEntry
    }

    fun getDateLastEntry(): Instant?{
        return _lastDateEntry.value
    }

    fun setLastDateEntry(date: Instant){
        _lastDateEntry.postValue(date)
    }

    fun setLastDateEntryCount(count: Int = 0){
        var cnt: Int = 0
        cnt = _lastDateEntryCount.value!!
        _lastDateEntryCount.postValue(count  + cnt)
    }

    fun setThingSpeakData(feeds: List<Feed>){
        _thingSpeakData.postValue(feeds)
    }

    fun setRequestDone(selectedDate: Instant = Instant.now(),
                       timeframe: Int = DAILY_TIMEFRAME){
        Log.i(TAG, "processing setRequestDone")
        _isLoading.postValue(false)
        // NOTE: always do this at the end of response to ensure
        // right timing of sending updates to all receiver
        var dates: MutableList<Pair<Int,LocalDate>> = mutableListOf<Pair<Int, LocalDate>>().apply{}

        if(_isDone.isInitialized) {
            dates = _isDone.value!!.dates
        }
        dates.add(timeframe to instantToLocalDate(selectedDate))
        _isDone.postValue(RequestDone(Instant.now(), selectedDate, dates))
    }

}

data class RequestDone( val updateAt: Instant,
                        val selectedDate: Instant,
                        val dates: MutableList<Pair<Int, LocalDate>>)

class ThingSpeakViewModelFactory(private val repository: ThingSpeakRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThingSpeakViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThingSpeakViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}