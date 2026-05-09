package com.pallab.pumpmanager.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pallab.pumpmanager.feature.inventory.InventoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class LowStockWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val inventoryRepository: InventoryRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val lowStockTanks = inventoryRepository.getLowStockTanks().first()
        if (lowStockTanks.isEmpty()) return Result.success()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return Result.success()
        }

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val channelId = "low_stock"

        val channel = NotificationChannel(
            channelId,
            "Low Stock Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val messages = lowStockTanks.joinToString("\n") { tank ->
            val fuelLabel = tank.fuelTypeId.replaceFirstChar { it.uppercase() }
            "${fuelLabel}: ${tank.currentStockLiters.toInt()} / ${tank.capacityLiters.toInt()} L"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Low Stock Alert")
            .setContentText("${lowStockTanks.size} tank(s) below 10%")
            .setStyle(NotificationCompat.BigTextStyle().bigText(messages))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "low_stock_check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<LowStockWorker>(2, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
