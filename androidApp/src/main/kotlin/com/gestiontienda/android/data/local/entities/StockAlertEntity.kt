package com.gestiontienda.android.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.gestiontienda.android.domain.model.AlertType
import com.gestiontienda.android.domain.model.StockAlert
import java.util.Date

@Entity(
    tableName = "stock_alerts",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StockAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val currentStock: Int,
    val minStock: Int,
    val alertType: AlertType,
    val createdAt: Date,
    val isRead: Boolean,
) {
    fun toStockAlert() = StockAlert(
        id = id,
        productId = productId,
        productName = productName,
        currentStock = currentStock,
        minStock = minStock,
        alertType = alertType,
        createdAt = createdAt,
        isRead = isRead
    )

    companion object {
        fun fromStockAlert(alert: StockAlert) = StockAlertEntity(
            id = alert.id,
            productId = alert.productId,
            productName = alert.productName,
            currentStock = alert.currentStock,
            minStock = alert.minStock,
            alertType = alert.alertType,
            createdAt = alert.createdAt,
            isRead = alert.isRead
        )
    }
} 
