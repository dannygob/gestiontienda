package com.gestiontienda.android.data.local.dao

import androidx.room.*
import com.gestiontienda.android.data.local.entities.StockAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockAlertDao {
    @Query("SELECT * FROM stock_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<StockAlertEntity>>

    @Query("SELECT * FROM stock_alerts WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadAlerts(): Flow<List<StockAlertEntity>>

    @Query("SELECT COUNT(*) FROM stock_alerts WHERE isRead = 0")
    fun getUnreadAlertsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: StockAlertEntity)

    @Update
    suspend fun updateAlert(alert: StockAlertEntity)

    @Delete
    suspend fun deleteAlert(alert: StockAlertEntity)

    @Query("UPDATE stock_alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAsRead(alertId: Long)

    @Query("UPDATE stock_alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM stock_alerts WHERE createdAt < :timestamp")
    suspend fun deleteOldAlerts(timestamp: Long)

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM stock_alerts 
            WHERE productId = :productId 
            AND alertType = :alertType 
            AND createdAt > :since
        )
    """
    )
    suspend fun hasRecentAlert(productId: Long, alertType: String, since: Long): Boolean
} 
