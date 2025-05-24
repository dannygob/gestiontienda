package com.gestiontienda.android.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.gestiontienda.android.MainActivity
import com.gestiontienda.android.R
import com.gestiontienda.android.domain.model.AlertType
import com.gestiontienda.android.domain.model.StockAlert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockNotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager,
) {
    companion object {
        const val CHANNEL_ID = "stock_alerts"
        const val CHANNEL_NAME = "Stock Alerts"
        const val CHANNEL_DESCRIPTION = "Notificaciones de alertas de stock"
        const val REQUEST_CODE = 0
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(alert: StockAlert) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (alert.alertType) {
            AlertType.LOW_STOCK -> "Stock Bajo"
            AlertType.OUT_OF_STOCK -> "Sin Stock"
            AlertType.EXPIRING_SOON -> "Próximo a Vencer"
            AlertType.EXPIRED -> "Producto Vencido"
        }

        val message = when (alert.alertType) {
            AlertType.LOW_STOCK -> "${alert.productName} tiene stock bajo (${alert.currentStock}/${alert.minStock})"
            AlertType.OUT_OF_STOCK -> "${alert.productName} está sin stock"
            AlertType.EXPIRING_SOON -> "${alert.productName} está próximo a vencer"
            AlertType.EXPIRED -> "${alert.productName} ha vencido"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(alert.id.toInt(), notification)
    }

    fun cancelNotification(alertId: Int) {
        notificationManager.cancel(alertId)
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
} 
