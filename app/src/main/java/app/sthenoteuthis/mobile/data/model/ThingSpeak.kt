package app.sthenoteuthis.mobile.data.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import androidx.room.TypeConverter
import androidx.room.TypeConverters

data class Channel(
    @SerializedName("id")
    @Expose
    val id: Int? = null)
{
    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("description")
    @Expose
    var description: String? = null

    @SerializedName("latitude")
    @Expose
    var latitude: String? = null

    @SerializedName("longitude")
    @Expose
    var longitude: String? = null

    @SerializedName("field1")
    @Expose
    var field1: String? = null

    @SerializedName("field2")
    @Expose
    var field2: String? = null

    @SerializedName("field3")
    @Expose
    var field3: String? = null

    @SerializedName("field4")
    @Expose
    var field4: String? = null

    @SerializedName("field5")
    @Expose
    var field5: String? = null

    @SerializedName("field6")
    @Expose
    var field6: String? = null

    @SerializedName("field7")
    @Expose
    var field7: String? = null

    @SerializedName("field8")
    @Expose
    var field8: String? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("last_entry_id")
    @Expose
    var lastEntryId: Int? = null
}

data class Feed(
    @SerializedName("entry_id")
    @Expose
    val entryId: Int? = null
) {
    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("field1")
    @Expose
    var pH: Float? = null

    @SerializedName("field2")
    @Expose
    var temperature: Float? = null

    @SerializedName("field3")
    @Expose
    var salinity: Float? = null

    @SerializedName("field4")
    @Expose
    var dissolvedOxygen: Float? = null

    @SerializedName("field5")
    @Expose
    var tds: Float? = null

    @SerializedName("field6")
    @Expose
    var turbidity: Float? = null

    @SerializedName("field7")
    @Expose
    var field7: Float? = null

    @SerializedName("field8")
    @Expose
    var field8: Float? = null
}

data class ThingSpeak(
    @SerializedName("channel")
    @Expose
    val channel: Channel? = null,

    @SerializedName("feeds")
    @Expose
    val feeds: List<Feed>? = null
)


@Entity(tableName = "water_quality_feed")
@TypeConverters(InstantStringConverter::class)
data class FeedEntity(
    @PrimaryKey @ColumnInfo(name = "entry_id") val entryId: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "ph") val pH: Float? = null,
    @ColumnInfo(name = "temperature") val temperature: Float? = null,
    @ColumnInfo(name = "salinity") val salinity: Float? = null,
    @ColumnInfo(name = "dissolved_oxygen") val dissolvedOxygen: Float? = null,
    @ColumnInfo(name = "tds") val tds: Float? = null,
    @ColumnInfo(name = "turbidity") val turbidity: Float? = null
)

@Dao
interface FeedEntityDao {
    @Query("SELECT * FROM water_quality_feed")
    fun getAll(): Flow<List<FeedEntity>>

    @Query("SELECT * FROM water_quality_feed WHERE created_at >= :dateSince AND created_at <= :dateUntil")
    fun findByDateRange(dateSince: String, dateUntil: String): Flow<List<FeedEntity>>

    @Query("SELECT COUNT(*) FROM water_quality_feed WHERE created_at >= :dateSince AND created_at <= :dateUntil")
    suspend fun size(dateSince: String, dateUntil: String): Int

    @Query("SELECT COUNT(*) FROM water_quality_feed")
    suspend fun size(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feed: FeedEntity)

    @Update
    suspend fun update(feed: FeedEntity)

    @Delete
    suspend fun delete(feeds: FeedEntity)
}

fun Feed.toFeedEntity(): FeedEntity {
    return FeedEntity(this.entryId.toString(), Instant.parse(this.createdAt),
        this.pH, this.temperature, this.salinity, this.dissolvedOxygen, this.tds, this.turbidity)
}

fun Feed.isGood(): Boolean{
    //TODO: check the status
    return true
}

fun FeedEntity.toFeed(): Feed {
    val feed = Feed(this.entryId.toInt())
    feed.createdAt = this.createdAt.toString()
    feed.pH = this.pH
    feed.temperature = this.temperature
    feed.salinity = this.salinity
    feed.dissolvedOxygen = this.dissolvedOxygen
    feed.tds = this.tds
    feed.turbidity = this.turbidity

    return feed
}

object InstantStringConverter {
    @TypeConverter
    @JvmStatic
    fun fromInstant(instant: Instant?): String? {
        return instant?.toString()
    }

    @TypeConverter
    @JvmStatic
    fun toInstant(instantString: String?): Instant? {
        return instantString?.let { Instant.parse(it) }
    }
}