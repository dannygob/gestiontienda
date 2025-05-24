package com.gestiontienda.android.domain.model

import java.util.Date

data class StockAlert(
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val currentStock: Int,
    val minStock: Int,
    val alertType: AlertType,
    val createdAt: Date = Date(),
    val isRead: Boolean = false,
)

enum class AlertType {
    LOW_STOCK,      // Stock below minimum threshold
    OUT_OF_STOCK,   // Stock is zero
    EXPIRING_SOON,  // Product is near expiration date
    EXPIRED         // Product has expired
} 
