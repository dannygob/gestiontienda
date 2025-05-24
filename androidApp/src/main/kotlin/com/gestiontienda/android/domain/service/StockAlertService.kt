package com.gestiontienda.android.domain.service

import com.gestiontienda.android.domain.model.StockAlert
import kotlinx.coroutines.flow.Flow

interface StockAlertService {
    fun getAllAlerts(): Flow<List<StockAlert>>
    fun getUnreadAlerts(): Flow<List<StockAlert>>
    fun getUnreadAlertsCount(): Flow<Int>
    suspend fun createAlert(alert: StockAlert)
    suspend fun markAsRead(alertId: Long)
    suspend fun markAllAsRead()
    suspend fun deleteAlert(alert: StockAlert)
    suspend fun checkLowStockProducts()
    suspend fun checkExpiringProducts()
    suspend fun cleanupOldAlerts(maxAgeDays: Int)
} 
