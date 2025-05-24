package com.gestiontienda.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gestiontienda.android.domain.model.Customer
import com.gestiontienda.android.domain.model.CustomerStatus
import java.util.Date

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
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
) {
    fun toCustomer() = Customer(
        id = id,
        name = name,
        email = email,
        phone = phone,
        address = address,
        taxId = taxId,
        loyaltyPoints = loyaltyPoints,
        creditLimit = creditLimit,
        currentCredit = currentCredit,
        lastPurchaseDate = lastPurchaseDate,
        totalPurchases = totalPurchases,
        purchaseCount = purchaseCount,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromCustomer(customer: Customer) = CustomerEntity(
            id = customer.id,
            name = customer.name,
            email = customer.email,
            phone = customer.phone,
            address = customer.address,
            taxId = customer.taxId,
            loyaltyPoints = customer.loyaltyPoints,
            creditLimit = customer.creditLimit,
            currentCredit = customer.currentCredit,
            lastPurchaseDate = customer.lastPurchaseDate,
            totalPurchases = customer.totalPurchases,
            purchaseCount = customer.purchaseCount,
            status = customer.status,
            createdAt = customer.createdAt,
            updatedAt = customer.updatedAt
        )
    }
} 