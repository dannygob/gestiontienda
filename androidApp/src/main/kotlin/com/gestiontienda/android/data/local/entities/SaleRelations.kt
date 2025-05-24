package com.gestiontienda.android.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class SaleWithItems(
    @Embedded
    val sale: SaleEntity,
    @Relation(
        entity = SaleItemEntity::class,
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemWithProduct>,
)

data class SaleItemWithProduct(
    @Embedded
    val saleItem: SaleItemEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity,
) 