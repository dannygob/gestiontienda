package com.gestiontienda.android.data.repository

import com.gestiontienda.android.data.local.dao.SaleDao
import com.gestiontienda.android.data.local.dao.SaleWithSummary
import com.gestiontienda.android.data.local.dao.SalesSummary
import com.gestiontienda.android.data.local.entities.*
import com.gestiontienda.android.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepositoryImpl @Inject constructor(
    private val saleDao: SaleDao,
) : SaleRepository {

    override fun getAllSales(): Flow<List<SaleWithItems>> {
        return saleDao.getAllSales()
    }

    override fun getSalesByDateRange(startDate: Date, endDate: Date): Flow<List<SaleWithItems>> {
        return saleDao.getSalesByDateRange(startDate, endDate)
    }

    override fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>> {
        return saleDao.getSaleItems(saleId)
    }

    override fun getRecentSales(limit: Int): Flow<List<SaleWithSummary>> {
        return saleDao.getRecentSales(limit)
    }

    override fun getSalesSummary(startDate: Date, endDate: Date): Flow<SalesSummary> {
        return saleDao.getSalesSummary(startDate, endDate)
    }

    override suspend fun createSale(sale: SaleEntity, items: List<SaleItemEntity>): Result<Long> {
        return try {
            val saleId = saleDao.insertSaleWithItems(sale, items)
            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSale(sale: SaleEntity) {
        saleDao.updateSale(sale)
    }

    override suspend fun deleteSale(sale: SaleEntity) {
        saleDao.deleteSale(sale)
    }

    override suspend fun getDailySalesStats(startDate: Date, endDate: Date): List<SalesSummary> {
        // Calculate daily stats for each day in the range
        val stats = mutableListOf<SalesSummary>()
        var currentDate = startDate
        while (!currentDate.after(endDate)) {
            val dailyStats = saleDao.getDailySalesStats(currentDate)
            stats.add(dailyStats)
            // Move to next day
            currentDate = Date(currentDate.time + 24 * 60 * 60 * 1000)
        }
        return stats
    }
} 
