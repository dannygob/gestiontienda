package com.gestiontienda.android.domain.model

import java.util.Date

data class Customer(
    val id: Long = 0,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val taxId: String?,
    val loyaltyPoints: Int,
    val creditLimit: Double,
    val currentCredit: Double,
    val lastPurchaseDate: Date?,
    val totalPurchases: Double,
    val purchaseCount: Int,
    val status: CustomerStatus,
    val createdAt: Date,
    val updatedAt: Date,
)

data class CustomerCredit(
    val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val remainingAmount: Double,
    val dueDate: Date,
    val status: CreditStatus,
    val notes: String?,
    val createdAt: Date = Date(),
)

data class CustomerPurchase(
    val saleId: Long,
    val date: Date,
    val total: Double,
    val paymentMethod: String,
    val pointsEarned: Int,
    val creditUsed: Double = 0.0,
)

data class LoyaltyConfig(
    val pointsPerCurrency: Double = 1.0,
    val minimumForRedemption: Int = 100,
    val redemptionRate: Double = 0.01,
    val expirationMonths: Int = 12,
)

enum class CreditStatus {
    ACTIVE,
    PAID,
    OVERDUE,
    CANCELLED
} 
