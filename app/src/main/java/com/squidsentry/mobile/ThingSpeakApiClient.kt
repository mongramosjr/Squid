package com.squidsentry.mobile


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.thingspeak.com"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}
object ThingSpeakApiClient {
    val apiService: ThingSpeakService by lazy { RetrofitClient.retrofit.create(ThingSpeakService::class.java) }
}