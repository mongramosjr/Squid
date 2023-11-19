package com.squidsentry.mobile

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.DateFormat

interface ThingSpeakService {
    @GET("/channels/2210889/feeds.json")
    fun getLast(@Query("results") entries: Int = 288): Call<ThingSpeak>

    @GET("/channels/2210889/feeds.json")
    fun getData(@Query("start") start: String,
                @Query("end") end: String,
                @Query("results") entries: Int = 280,
                @Query("ave") ave: Int = 10): Call<ThingSpeak>
}