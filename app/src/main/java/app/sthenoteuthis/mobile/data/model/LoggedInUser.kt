package app.sthenoteuthis.mobile.data.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data class that captures user information for logged in users retrieved from GoogleLoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String,
    val credential: String
)

@Entity(tableName = "logged_in_account")
data class LoggedInAccount(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "account_id") val accountId: String?,
    @ColumnInfo(name = "display_name") val displayName: String?,
    @ColumnInfo(name = "credential") val credential: String?
)

@Dao
interface LoggedInAccountDao {
    @Query("SELECT * FROM logged_in_account")
    fun getAll(): Flow<List<LoggedInAccount>>

    @Query("SELECT * FROM logged_in_account WHERE uid IN (:uids)")
    suspend fun loadAllByIds(uids: IntArray): List<LoggedInAccount>

    @Query("SELECT * FROM logged_in_account WHERE display_name LIKE :displayName LIMIT 1")
    suspend fun findByName(displayName: String): LoggedInAccount

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(vararg accounts: LoggedInAccount)

    @Update
    suspend fun updateAccounts(vararg accounts: LoggedInAccount)


    @Delete
    suspend fun delete(accounts: LoggedInAccount)
}


