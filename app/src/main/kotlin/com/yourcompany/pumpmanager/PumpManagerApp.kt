package com.yourcompany.pumpmanager

import android.app.Application
import com.yourcompany.pumpmanager.core.security.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PumpManagerApp : Application() {

    @Inject lateinit var databaseSeeder: DatabaseSeeder

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            databaseSeeder.seedIfEmpty()
        }
    }
}
