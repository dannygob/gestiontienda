package com.gestiontienda.android.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long?, // This is nullable, but still needs a value (either a Long or null)
    val total: Double, // Needs a value
    val subtotal: Double, // Needs a value
    val tax: Double, // Needs a value
    val discount: Double, // Needs a value
    val paymentMethod: String, // Needs a value
    val status: String, // Needs a value
    val notes: String?, // This is nullable and has a default value (null) if not provided
    val createdAt: Date = Date(), // Has a default value
    val updatedAt: Date = Date(), // Has a default value
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("saleId"),
        Index("productId")
    ]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val quantity: Int,
    val priceAtSale: Double,
    val discount: Double = 0.0,
    val notes: String? = null,
)

enum class PaymentMethod {
    CASH,
    CARD,
    TRANSFER,
    OTHER
}

enum class SaleStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    REFUNDED
} 
