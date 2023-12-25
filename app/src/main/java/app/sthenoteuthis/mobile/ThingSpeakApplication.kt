package app.sthenoteuthis.mobile

import android.app.Application
import app.sthenoteuthis.mobile.data.SquidDatabase
import app.sthenoteuthis.mobile.data.ThingSpeakApiRepository
import app.sthenoteuthis.mobile.data.ThingSpeakRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


class ThingSpeakApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts

    val database by lazy { SquidDatabase.getDatabase( this, CoroutineScope(SupervisorJob()) ) }
    val repository by lazy { ThingSpeakRepository(
        database.feedEntityDao(), ThingSpeakApiRepository()
    ) }
}