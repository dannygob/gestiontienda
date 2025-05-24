package com.gestiontienda.android.domain.repository

import com.gestiontienda.android.data.local.dao.SaleWithSummary
import com.gestiontienda.android.data.local.dao.SalesSummary
import com.gestiontienda.android.data.local.entities.SaleEntity
import com.gestiontienda.android.data.local.entities.SaleItemEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface SaleRepository {
    fun getAllSales(): Flow<List<SaleEntity>>
    fun getSalesByDateRange(startDate: Date, endDate: Date): Flow<List<SaleEntity>>
    fun getSaleItems(saleId: Long): Flow<List<SaleItemEntity>>
    fun getRecentSales(limit: Int): Flow<List<SaleWithSummary>>
    fun getSalesSummary(startDate: Date, endDate: Date): Flow<SalesSummary>
    suspend fun createSale(sale: SaleEntity, items: List<SaleItemEntity>): Result<Long>
    suspend fun updateSale(sale: SaleEntity)
    suspend fun deleteSale(sale: SaleEntity)
    suspend fun getDailySalesStats(startDate: Date, endDate: Date): List<SalesSummary>
}
