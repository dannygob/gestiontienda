package com.gestiontienda.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gestiontienda.android.data.local.entities.PeriodStatsEntity
import com.gestiontienda.android.data.local.entities.ProductSalesStatsEntity
import com.gestiontienda.android.data.local.entities.SaleEntity
import com.gestiontienda.android.data.local.entities.SaleItemEntity
import com.gestiontienda.android.data.local.entities.SaleWithItems
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): SaleWithItems?

    @Transaction
    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCustomerSales(customerId: Long): Flow<List<SaleWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Query(
        """ 
    SELECT 
        SUM(s.total) as totalSales, 
        COUNT(DISTINCT s.id) as saleCount, 
        SUM(si.quantity) as totalItems 
    FROM sales s 
    LEFT JOIN sale_items si ON s.id = si.saleId 
    WHERE s.createdAt BETWEEN :startDate AND :endDate 
    """
    )
    fun getSalesStats(startDate: Date, endDate: Date): Flow<SalesSummary>

    @Transaction
    suspend fun insertSaleWithItems(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val saleId = insertSale(sale)
        insertSaleItems(items.map { it.copy(saleId = saleId) })
        return saleId
    }

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("UPDATE sales SET status = :status WHERE id = :saleId")
    suspend fun updateSaleStatus(saleId: Long, status: String)

    @Query("SELECT * FROM period_stats")
    fun getPeriodStats(): Flow<PeriodStatsEntity>

    @Query("SELECT * FROM product_sales_stats ORDER BY totalRevenue DESC")
    fun getProductSalesStats(): Flow<List<ProductSalesStatsEntity>>

    @Transaction
    @Query(
        """
        SELECT * FROM sales 
        WHERE createdAt >= :startDate 
        AND createdAt <= :endDate 
        ORDER BY createdAt DESC
    """
    )
    fun getSalesInPeriod(startDate: Date, endDate: Date): Flow<List<SaleWithItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM sales 
        WHERE createdAt >= :startDate 
        AND createdAt <= :endDate 
        ORDER BY createdAt DESC
    """
    )
    fun getSalesByDateRange(startDate: Date, endDate: Date): Flow<List<SaleWithItems>>

    @Transaction
    @Query(
        """
        SELECT s.*, 
            COUNT(si.id) as itemCount,
            SUM(si.quantity) as totalQuantity
        FROM sales s
        LEFT JOIN sale_items si ON s.id = si.saleId
        GROUP BY s.id
        ORDER BY s.createdAt DESC
        LIMIT :limit
    """
    )
    fun getRecentSales(limit: Int): Flow<List<SaleWithSummary>>

    @Query(
        """
        SELECT 
            SUM(s.total) as totalSales,
            COUNT(DISTINCT s.id) as saleCount,
            SUM(si.quantity) as totalItems
        FROM sales s
        LEFT JOIN sale_items si ON s.id = si.saleId
        WHERE s.createdAt BETWEEN :startDate AND :endDate
    """
    )
    fun getSalesSummary(startDate: Date, endDate: Date): Flow<SalesSummary>

    @Query(
        """
        SELECT * FROM sale_items WHERE saleId = :saleId
    """
    )
    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>>

    @Query(
        """
        SELECT 
            strftime('%Y-%m', createdAt) as period,
            COUNT(*) as saleCount,
            SUM(total) as totalAmount,
            AVG(total) as averageAmount
        FROM sales
        WHERE createdAt >= :startDate AND createdAt <= :endDate
        GROUP BY strftime('%Y-%m', createdAt)
        ORDER BY period DESC
    """
    )
    fun getMonthlySalesStats(startDate: Date, endDate: Date): Flow<List<MonthlySalesStats>>

    @Query(
        """
        SELECT 
            SUM(s.total) as totalSales,
            COUNT(DISTINCT s.id) as saleCount,
            SUM(si.quantity) as totalItems
        FROM sales s
        LEFT JOIN sale_items si ON s.id = si.saleId
        WHERE DATE(s.createdAt) = DATE(:date)
        GROUP BY DATE(s.createdAt)
    """
    )
    suspend fun getDailySalesStats(date: Date): SalesSummary
}

data class MonthlySalesStats(
    val period: String,
    val saleCount: Int,
    val totalAmount: Double,
    val averageAmount: Double,
)

data class SaleWithSummary(
    @Embedded val sale: SaleEntity,
    val itemCount: Int,
    val totalQuantity: Int,
)

data class SalesSummary(
    val totalSales: Double,
    val saleCount: Int,
    val totalItems: Int,
) 
