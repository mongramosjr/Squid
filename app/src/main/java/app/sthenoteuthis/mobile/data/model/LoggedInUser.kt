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
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Data class that captures user information for logged in users retrieved from GoogleLoginRepository
 */
data class LoggedInUser(
    val uid: String
){
    var displayName: String? = null
    var email: String? = null
    var phoneNumber: String? = null
    var credential: String? = null
    var providerId: String? = null
}

@Entity(tableName = "logged_in_account")
data class LoggedInAccount(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "display_name") val displayName: String?,
    @ColumnInfo(name = "provider_id") val providerId: String?,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "phone_number") val phoneNumber: String?
)

@Dao
interface LoggedInAccountDao {
    @Query("SELECT * FROM logged_in_account")
    fun getAll(): Flow<List<LoggedInAccount>>

    @Query("SELECT COUNT(*) FROM logged_in_account")
    suspend fun size(): Int

    @Query("SELECT * FROM logged_in_account WHERE display_name LIKE :displayName LIMIT 1")
    suspend fun findByName(displayName: String): LoggedInAccount

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: LoggedInAccount)

    @Update
    suspend fun update(account: LoggedInAccount)


    @Delete
    suspend fun delete(accounts: LoggedInAccount)
}
fun FirebaseUser.toLoggedInAccount(): LoggedInAccount{
    return LoggedInAccount(this.uid, this.displayName, this.providerId, this.email, this.phoneNumber)
}

