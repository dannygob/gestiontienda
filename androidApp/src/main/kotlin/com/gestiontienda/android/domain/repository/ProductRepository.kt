package com.gestiontienda.android.domain.repository

import com.gestiontienda.android.data.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<ProductEntity>>
    fun searchProducts(query: String): Flow<List<ProductEntity>>
    suspend fun getProductById(id: Long): ProductEntity?
    suspend fun getProductByBarcode(barcode: String): ProductEntity?
    suspend fun insertProduct(product: ProductEntity)
    suspend fun updateProduct(product: ProductEntity)
    suspend fun deleteProduct(product: ProductEntity)
    suspend fun updateStock(productId: Long, newStock: Int)
} 
