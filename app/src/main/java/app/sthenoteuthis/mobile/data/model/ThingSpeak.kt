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
import java.util.Date
import androidx.room.TypeConverter
import androidx.room.TypeConverters

data class Channel(
    @SerializedName("id")
    @Expose
    val id: Int? = null)
{
    @SerializedName("name")
    @Expose
    val name: String? = null

    @SerializedName("description")
    @Expose
    val description: String? = null

    @SerializedName("latitude")
    @Expose
    val latitude: String? = null

    @SerializedName("longitude")
    @Expose
    val longitude: String? = null

    @SerializedName("field1")
    @Expose
    val field1: String? = null

    @SerializedName("field2")
    @Expose
    val field2: String? = null

    @SerializedName("field3")
    @Expose
    val field3: String? = null

    @SerializedName("field4")
    @Expose
    val field4: String? = null

    @SerializedName("field5")
    @Expose
    val field5: String? = null

    @SerializedName("field6")
    @Expose
    val field6: String? = null

    @SerializedName("field7")
    @Expose
    val field7: String? = null

    @SerializedName("created_at")
    @Expose
    val createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    val updatedAt: String? = null

    @SerializedName("last_entry_id")
    @Expose
    val lastEntryId: Int? = null
}

data class Feed(
    @SerializedName("entry_id")
    @Expose
    val entryId: Int? = null
) {
    @SerializedName("created_at")
    @Expose
    val createdAt: String? = null

    @SerializedName("field1")
    @Expose
    val field1: Float? = null

    @SerializedName("field2")
    @Expose
    val field2: Float? = null

    @SerializedName("field3")
    @Expose
    val field3: Float? = null

    @SerializedName("field4")
    @Expose
    val field4: Float? = null

    @SerializedName("field5")
    @Expose
    val field5: Float? = null

    @SerializedName("field6")
    @Expose
    val field6: Float? = null

    @SerializedName("field7")
    @Expose
    val field7: Float? = null
}

data class ThingSpeak(
    @SerializedName("channel")
    @Expose
    val channel: Channel? = null
) {
    @SerializedName("feeds")
    @Expose
    val feeds: List<Feed>? = null
}


@Entity(tableName = "water_quality_feed")
@TypeConverters(InstantStringConverter::class)
data class FeedEntity(
    @PrimaryKey @ColumnInfo(name = "entry_id") val entryId: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "field1") val pH: Float? = null,
    @ColumnInfo(name = "field2") val temperature: Float? = null,
    @ColumnInfo(name = "field3") val salinity: Float? = null,
    @ColumnInfo(name = "field4") val dissolvedOxygen: Float? = null,
    @ColumnInfo(name = "field5") val tds: Float? = null,
    @ColumnInfo(name = "field6") val turbidity: Float? = null
)

@Dao
interface FeedEntityDao {
    @Query("SELECT * FROM water_quality_feed")
    fun getAll(): Flow<List<FeedEntity>>

    @Query("SELECT * FROM water_quality_feed WHERE created_at >= :dateSince AND created_at <= :dateUntil")
    fun findByDateRange(dateSince: String, dateUntil: String): Flow<List<FeedEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feed: FeedEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeds(vararg feeds: FeedEntity)

    @Update
    suspend fun updateFeeds(vararg feeds: FeedEntity)

    @Delete
    suspend fun delete(feeds: FeedEntity)
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