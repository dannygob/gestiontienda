package com.gestiontienda.repository

import com.gestiontienda.api.OpenFoodFactsApi
import com.gestiontienda.api.OpenFoodFactsProduct
import com.gestiontienda.db.Product
import com.gestiontienda.db.StoreDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class ProductRepository(
    private val database: StoreDatabase,
    private val api: OpenFoodFactsApi,
) {
    // Local database operations
    fun getAllProducts(): Flow<List<Product>> =
        database.getAllProducts()

    fun getProductById(id: Long): Product? =
        database.getProductById(id)

    fun getProductByCode(code: String): Product? =
        database.getProductByCode(code)

    // Combined operations with Open Food Facts
    suspend fun addProductByBarcode(
        barcode: String,
        purchasePrice: Double,
        salePrice: Double,
        stock: Long = 0,
        minStock: Long = 0,
    ): Result<Product> {
        // Try to get product info from Open Food Facts
        return api.getProduct(barcode).fold(
            onSuccess = { offProduct ->
                // Insert into local database
                database.insertProduct(
                    name = offProduct.product.product_name ?: "Unknown Product",
                    code = barcode,
                    purchasePrice = purchasePrice,
                    salePrice = salePrice,
                    stock = stock,
                    minStock = minStock
                )
                // Return the newly inserted product
                Result.success(database.getProductByCode(barcode)!!)
            },
            onFailure = {
                // If API fails, create a basic product entry
                database.insertProduct(
                    name = "Product #$barcode",
                    code = barcode,
                    purchasePrice = purchasePrice,
                    salePrice = salePrice,
                    stock = stock,
                    minStock = minStock
                )
                Result.success(database.getProductByCode(barcode)!!)
            }
        )
    }

    suspend fun searchProducts(query: String): Result<List<OpenFoodFactsProduct>> =
        api.searchProducts(query)

    suspend fun updateStock(id: Long, stockChange: Long) {
        database.updateProductStock(id, stockChange)
    }

    fun getLowStockProducts(): Flow<List<Product>> =
        database.getLowStockProducts()
} 
