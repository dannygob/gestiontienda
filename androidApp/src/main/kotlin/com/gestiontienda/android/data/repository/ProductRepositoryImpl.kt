package com.gestiontienda.android.data.repository

import com.gestiontienda.android.data.local.dao.ProductDao
import com.gestiontienda.android.data.local.entities.ProductEntity
import com.gestiontienda.android.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
) : ProductRepository {

    override fun getAllProducts(): Flow<List<ProductEntity>> =
        productDao.getAllProducts()

    override fun searchProducts(query: String): Flow<List<ProductEntity>> = flow {
        val searchQuery = if (query.isBlank()) "" else "%${query.trim()}%"
        emit(productDao.searchProducts(searchQuery))
    }

    override suspend fun getProductById(id: Long): ProductEntity? =
        productDao.getProductById(id)

    override suspend fun getProductByBarcode(barcode: String): ProductEntity? =
        productDao.getProductByBarcode(barcode)

    override suspend fun insertProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    override suspend fun updateProduct(product: ProductEntity) =
        productDao.updateProduct(product)

    override suspend fun deleteProduct(product: ProductEntity) =
        productDao.deleteProduct(product)

    override suspend fun updateStock(productId: Long, newStock: Int) =
        productDao.updateStock(productId, newStock)
} 
