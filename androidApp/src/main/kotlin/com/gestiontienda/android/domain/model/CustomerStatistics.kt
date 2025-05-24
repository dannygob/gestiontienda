package com.gestiontienda.android.domain.model

import java.util.Date

data class CustomerStatistics(
    val totalPurchases: Double = 0.0,
    val purchaseCount: Int = 0,
    val averagePurchase: Double = 0.0,
    val totalLoyaltyPoints: Int = 0,
    val totalCreditUsed: Double = 0.0,
    val currentCredit: Double = 0.0,
    val lastPurchaseDate: Date? = null,
)

data class CustomerWithStats(
    val customer: Customer,
    val statistics: CustomerStatistics,
) 