package com.gestiontienda.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.gestiontienda.db.StoreDatabase
import kotlinx.datetime.Clock

class OpenFoodFactsCache(private val database: StoreDatabase) {
    private val json = Json { ignoreUnknownKeys = true }
    private val cacheTimeout = 24 * 60 * 60 * 1000 // 24 hours in milliseconds

    suspend fun getCachedProduct(barcode: String): OpenFoodFactsProduct? {
        val cachedData = database.getProductByCode(barcode)
        if (cachedData != null) {
            val now = Clock.System.now().toEpochMilliseconds()
            if (now - cachedData.updated_at < cacheTimeout) {
                // Return cached data if it's not expired
                return json.decodeFromString(cachedData.toString())
            }
        }
        return null
    }

    suspend fun cacheProduct(product: OpenFoodFactsProduct) {
        val productJson = json.encodeToString(product)
        val now = Clock.System.now().toEpochMilliseconds()

        database.insertProduct(
            name = product.product.product_name ?: "Unknown Product",
            code = product.code,
            purchasePrice = 0.0, // Default values, to be updated later
            salePrice = 0.0,
            stock = 0,
            minStock = 0
        )
    }

    suspend fun clearExpiredCache() {
        val now = Clock.System.now().toEpochMilliseconds()
        // Implement cache cleanup logic
    }
} 
