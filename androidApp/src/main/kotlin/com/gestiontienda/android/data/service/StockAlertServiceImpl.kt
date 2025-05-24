package com.gestiontienda.android.data.service

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.gestiontienda.android.R
import com.gestiontienda.android.data.local.dao.ProductDao
import com.gestiontienda.android.data.local.dao.StockAlertDao
import com.gestiontienda.android.data.local.entities.StockAlertEntity
import com.gestiontienda.android.domain.model.AlertType
import com.gestiontienda.android.domain.model.StockAlert
import com.gestiontienda.android.domain.service.StockAlertService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockAlertServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stockAlertDao: StockAlertDao,
    private val productDao: ProductDao,
    private val stockNotificationService: StockNotificationService,
) : StockAlertService {

    companion object {
        private const val MIN_ALERT_INTERVAL_HOURS = 24
    }

    override fun getAllAlerts(): Flow<List<StockAlert>> =
        stockAlertDao.getAllAlerts().map { entities ->
            entities.map { it.toStockAlert() }
        }

    override fun getUnreadAlerts(): Flow<List<StockAlert>> =
        stockAlertDao.getUnreadAlerts().map { entities ->
            entities.map { it.toStockAlert() }
        }

    override fun getUnreadAlertsCount(): Flow<Int> =
        stockAlertDao.getUnreadAlertsCount()

    override suspend fun createAlert(alert: StockAlert) {
        // Check if a similar alert was created recently
        val hasRecentAlert = stockAlertDao.hasRecentAlert(
            productId = alert.productId,
            alertType = alert.alertType.name,
            since = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(MIN_ALERT_INTERVAL_HOURS)
        )

        if (!hasRecentAlert) {
            stockAlertDao.insertAlert(StockAlertEntity.fromStockAlert(alert))
            stockNotificationService.showNotification(alert)
        }
    }

    override suspend fun markAsRead(alertId: Long) {
        stockAlertDao.markAsRead(alertId)
        stockNotificationService.cancelNotification(alertId.toInt())
    }

    override suspend fun markAllAsRead() {
        stockAlertDao.markAllAsRead()
        stockNotificationService.cancelAllNotifications()
    }

    override suspend fun deleteAlert(alert: StockAlert) {
        stockAlertDao.deleteAlert(StockAlertEntity.fromStockAlert(alert))
        stockNotificationService.cancelNotification(alert.id.toInt())
    }

    override suspend fun checkLowStockProducts() {
        productDao.getProductsWithLowStock().collect { products ->
            products.forEach { product ->
                if (product.stock <= 0) {
                    createAlert(
                        StockAlert(
                            productId = product.id,
                            productName = product.name,
                            currentStock = product.stock,
                            minStock = product.minStock,
                            alertType = AlertType.OUT_OF_STOCK
                        )
                    )
                } else if (product.stock <= product.minStock) {
                    createAlert(
                        StockAlert(
                            productId = product.id,
                            productName = product.name,
                            currentStock = product.stock,
                            minStock = product.minStock,
                            alertType = AlertType.LOW_STOCK
                        )
                    )
                }
            }
        }
    }

    override suspend fun checkExpiringProducts() {
        val thirtyDaysFromNow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 30)
        }.time

        productDao.getExpiringProducts(thirtyDaysFromNow).collect { products ->
            products.forEach { product ->
                val alertType = if (product.expirationDate.before(Date())) {
                    AlertType.EXPIRED
                } else {
                    AlertType.EXPIRING_SOON
                }

                createAlert(
                    StockAlert(
                        productId = product.id,
                        productName = product.name,
                        currentStock = product.stock,
                        minStock = product.minStock,
                        alertType = alertType
                    )
                )
            }
        }
    }

    override suspend fun cleanupOldAlerts(maxAgeDays: Int) {
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -maxAgeDays)
        }.timeInMillis

        stockAlertDao.deleteOldAlerts(cutoffDate)
    }
} 
