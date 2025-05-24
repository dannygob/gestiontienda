package com.gestiontienda.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String?,
    val barcode: String?,
    val categoryId: Long?,
    val price: Double,
    val purchasePrice: Double,
    val stock: Int,
    val reorderPoint: Int,
    val unit: String,
    val imageUrl: String?,
    val expirationDate: Date?,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
) 
