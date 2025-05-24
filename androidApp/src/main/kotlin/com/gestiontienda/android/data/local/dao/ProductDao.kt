package com.gestiontienda.android.data.local.dao

import androidx.room.*
import com.gestiontienda.android.data.local.entities.ProductEntity
import com.gestiontienda.android.data.local.entities.ProductSalesStatsEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= reorderPoint ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query(
        """
        SELECT * FROM products 
        WHERE name LIKE :query 
        OR barcode LIKE :query 
        ORDER BY 
            CASE 
                WHEN name LIKE :query || '%' THEN 1
                WHEN name LIKE '%' || :query || '%' THEN 2
                ELSE 3
            END,
            name ASC
    """
    )
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun updateStock(productId: Long, quantity: Int)

    @Query("SELECT * FROM products WHERE expirationDate <= :date AND stock > 0")
    fun getExpiringProducts(date: Date): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product_sales_stats WHERE quantitySold > 0 ORDER BY totalRevenue DESC")
    fun getTopSellingProducts(): Flow<List<ProductSalesStatsEntity>>
} 
