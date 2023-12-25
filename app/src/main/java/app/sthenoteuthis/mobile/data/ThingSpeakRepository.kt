package app.sthenoteuthis.mobile.data

import androidx.annotation.WorkerThread
import app.sthenoteuthis.mobile.data.model.FeedEntity
import app.sthenoteuthis.mobile.data.model.FeedEntityDao
import app.sthenoteuthis.mobile.data.model.InstantStringConverter
import app.sthenoteuthis.mobile.data.model.ThingSpeak
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import java.time.Instant

class ThingSpeakRepository(
    private val feedEntityDao: FeedEntityDao,
    private val thingSpeakApiRepository: ThingSpeakApiRepository) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allFeeds: Flow<List<FeedEntity>> = feedEntityDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(feed: FeedEntity) {
        feedEntityDao.insert(feed)
    }
    fun findByDateRange(dateSince: Instant, dateUntil: Instant): Flow<List<FeedEntity>> {
        val dateSinceString = InstantStringConverter.fromInstant(dateSince).toString()
        val dateUntilString = InstantStringConverter.fromInstant(dateUntil).toString()
        return feedEntityDao.findByDateRange(dateSinceString, dateUntilString)
    }

    fun fetchLastFeeds(entries: Int = 288): Call<ThingSpeak> {
        return thingSpeakApiRepository.getLast(entries)
    }
    fun fetchFeeds(start: String,
                end: String,
                ave: Int = 0,
                entries: Int = 8000): Call<ThingSpeak> {
        return thingSpeakApiRepository.getData(start, end, ave, entries)
    }



}