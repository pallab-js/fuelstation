package com.pallab.pumpmanager

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.pallab.pumpmanager.core.notification.LowStockWorker
import com.pallab.pumpmanager.core.security.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PumpManagerApp : Application(), Configuration.Provider {

    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var workFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            databaseSeeder.seedIfEmpty()
        }
        LowStockWorker.schedule(this)
    }
}
