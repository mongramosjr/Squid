package app.sthenoteuthis.mobile.data

import app.sthenoteuthis.mobile.data.model.ThingSpeak
import app.sthenoteuthis.mobile.data.model.ThingSpeakService
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

class ThingSpeakApiRepository() {

    fun getLast(entries: Int = 288): Call<ThingSpeak> {
        return ThingSpeakApiClient.apiService.getLast(entries)
    }
    fun getData(start: String,
                        end: String,
                        ave: Int = 0,
                        entries: Int = 8000): Call<ThingSpeak> {
        return ThingSpeakApiClient.apiService.getData(start, end, ave, entries)
    }
}

