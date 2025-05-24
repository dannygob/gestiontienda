package com.gestiontienda.android.domain.service

import com.gestiontienda.android.domain.model.ProductSalesStats
import com.gestiontienda.android.domain.model.SalesStatistics
import com.gestiontienda.android.domain.model.StatisticsPeriod
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface SalesStatisticsService {
    suspend fun getSalesStatistics(
        period: StatisticsPeriod,
        startDate: Date? = null,
        endDate: Date? = null,
    ): SalesStatistics

    fun getRealtimeSalesStatistics(
        period: StatisticsPeriod,
        startDate: Date? = null,
        endDate: Date? = null,
    ): Flow<SalesStatistics>

    suspend fun getTopSellingProducts(
        limit: Int = 10,
        startDate: Date? = null,
        endDate: Date? = null,
    ): List<ProductSalesStats>

    suspend fun getProductStatistics(
        productId: Long,
        period: StatisticsPeriod,
        startDate: Date? = null,
        endDate: Date? = null,
    ): ProductSalesStats

    suspend fun getSalesByPaymentMethod(
        startDate: Date? = null,
        endDate: Date? = null,
    ): Map<String, Double>

    suspend fun getSalesByHourDistribution(
        startDate: Date? = null,
        endDate: Date? = null,
    ): Map<Int, Double>

    suspend fun generateSalesReport(
        period: StatisticsPeriod,
        startDate: Date? = null,
        endDate: Date? = null,
        format: ReportFormat = ReportFormat.PDF,
    ): String // Returns the file path

    suspend fun exportSalesData(
        startDate: Date? = null,
        endDate: Date? = null,
        format: ExportFormat = ExportFormat.CSV,
    ): String // Returns the file path
}

enum class ReportFormat {
    PDF,
    EXCEL
}

enum class ExportFormat {
    CSV,
    EXCEL,
    JSON
} 
