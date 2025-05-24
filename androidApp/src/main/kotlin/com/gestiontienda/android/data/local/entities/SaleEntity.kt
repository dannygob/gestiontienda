package com.gestiontienda.android.data.local.entities

import androidx.room.*
import java.time.LocalDateTime
import java.util.Date

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long?,
    val total: Double,
    val subtotal: Double,
    val tax: Double,
    val discount: Double,
    val paymentMethod: String,
    val status: String,
    val notes: String?,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
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
