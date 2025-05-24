package com.gestiontienda.android.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.gestiontienda.android.domain.service.StockAlertService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class StockAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val stockAlertService: StockAlertService,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val WORK_NAME = "stock_alert_check"
        private const val MAX_ALERT_AGE_DAYS = 30

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val request = PeriodicWorkRequestBuilder<StockAlertWorker>(
                24, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // Flex period
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }

    override suspend fun doWork(): Result {
        try {
            // Check for low stock and expiring products
            stockAlertService.checkLowStockProducts()
            stockAlertService.checkExpiringProducts()

            // Clean up old alerts
            stockAlertService.cleanupOldAlerts(MAX_ALERT_AGE_DAYS)

            return Result.success()
        } catch (e: Exception) {
            return if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
} 
