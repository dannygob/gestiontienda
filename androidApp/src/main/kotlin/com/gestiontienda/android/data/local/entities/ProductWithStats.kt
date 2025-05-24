package com.gestiontienda.android.data.local.entities

import androidx.room.ColumnInfo
import java.util.Date

data class ProductWithStats(
    val id: Long,
    val name: String,
    val description: String?,
    val barcode: String?,
    val purchasePrice: Double,
    val price: Double,
    val stock: Int,
    val minStock: Int,
    val categoryId: Long?,
    val imageUrl: String?,
    val expirationDate: Date?,
    val createdAt: Date,
    val updatedAt: Date,
    @ColumnInfo(name = "totalSold")
    val totalSold: Int,
    @ColumnInfo(name = "averagePrice")
    val averagePrice: Double,
) 