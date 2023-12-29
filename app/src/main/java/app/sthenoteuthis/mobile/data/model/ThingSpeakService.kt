package app.sthenoteuthis.mobile.data.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val ThingSpeakDailyResult: Int = 288
const val ThingSpeakWeeklyResult: Int = 504
const val ThingSpeakMonthlylyResult: Int = 720
const val ThingSpeakYearlyResult: Int = 876
const val ThingSpeakMaximumResult: Int = 8000

const val ThingSpeakDailyAverage: Int = 0
const val ThingSpeakWeeklyAverage: Int = 4
const val ThingSpeakMonthlyAverage: Int = 12
const val ThingSpeakYearlyAverage: Int = 120



interface ThingSpeakService {
    @GET("/channels/2210889/feeds.json")
    fun getLast(@Query("results") entries: Int = ThingSpeakDailyResult): Call<ThingSpeak>

    
    /*
    The results parameter has the highest precedence. Using results with the
    parameters min, max, timescale, sum, average, or median can cause less
    than 8000 records to be returned. The results parameter determines
    the maximum number of entries to be used for a query, up to 8000.
    For example, consider a channel with one update per minute. A read request
    to that channel with the parameters ?results=120&sum=60 returns only two records,
    and not 120.
    start and end is in dateformat of "YYYY-MM-DD HH:NN:SS"
    daily average = 0 = 288 results
    weekly average = 4 = 504 results
    monthly average = 12 = 720 results
    yearly average = 120 = 876 results
     */
    @GET("/channels/2210889/feeds.json")
    fun getData(@Query("start") start: String,
                @Query("end") end: String,
                @Query("average") ave: Int = 0,
                @Query("results") entries: Int = ThingSpeakMaximumResult): Call<ThingSpeak>
}