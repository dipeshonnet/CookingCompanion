package com.example

import android.app.Application
import com.example.data.local.CookingDatabase
import com.example.data.repository.CookingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CookingApplication : Application() {

    // Singleton Database instance
    val database by lazy { CookingDatabase.getDatabase(this) }

    // Singleton Repository instance
    val repository by lazy { CookingRepository(database) }

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        // Run database seeding asynchronously so it doesn't block app startup
        applicationScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }
}
