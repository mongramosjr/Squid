package app.sthenoteuthis.mobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.sthenoteuthis.mobile.data.model.LoggedInAccount
import app.sthenoteuthis.mobile.data.model.LoggedInAccountDao
import kotlinx.coroutines.CoroutineScope


@Database(entities = [LoggedInAccount::class], version = 1)
abstract class SquidDatabase: RoomDatabase()  {

    abstract fun loggedInAccountDao(): LoggedInAccountDao

    companion object {
        @Volatile
        private var INSTANCE: SquidDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): SquidDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SquidDatabase::class.java,
                    "word_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this codelab.
                    //.fallbackToDestructiveMigration()
                    //.addCallback(WordDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

