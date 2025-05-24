package com.gestiontienda.android.data.service

import android.content.Context
import com.gestiontienda.android.data.local.dao.ProductDao
import com.gestiontienda.android.data.local.dao.SaleDao
import com.gestiontienda.android.data.local.entities.PeriodStatsEntity
import com.gestiontienda.android.data.local.entities.ProductSalesStatsEntity
import com.gestiontienda.android.domain.model.*
import com.gestiontienda.android.domain.service.ExportFormat
import com.gestiontienda.android.domain.service.ReportFormat
import com.gestiontienda.android.domain.service.SalesStatisticsService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class SalesStatisticsServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
) : SalesStatisticsService {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override suspend fun getSalesStatistics(
        period: StatisticsPeriod,
        startDate: Date?,
        endDate: Date?,
    ): SalesStatistics {
        val (start, end) = calculateDateRange(period, startDate, endDate)
        val previousStart = calculatePreviousPeriodStart(period, start)

        val currentStats = calculatePeriodStats(start, end)
        val previousStats = calculatePeriodStats(previousStart, start)

        return SalesStatistics(
            period = period,
            totalSales = currentStats.totalSales,
            totalItems = currentStats.totalItems,
            averageTicket = if (currentStats.totalItems > 0) {
                currentStats.totalSales / currentStats.totalItems
            } else 0.0,
            topProducts = getTopSellingProducts(10, start, end),
            salesByPaymentMethod = getSalesByPaymentMethod(start, end),
            salesByHour = getSalesByHourDistribution(start, end),
            startDate = start,
            endDate = end,
            profitMargin = currentStats.profitMargin,
            totalProfit = currentStats.totalProfit,
            comparisonWithPreviousPeriod = ComparisonStats(
                salesGrowth = calculateGrowth(previousStats.totalSales, currentStats.totalSales),
                itemsGrowth = calculateGrowth(
                    previousStats.totalItems.toDouble(),
                    currentStats.totalItems.toDouble()
                ),
                profitGrowth = calculateGrowth(previousStats.totalProfit, currentStats.totalProfit),
                averageTicketGrowth = calculateGrowth(
                    previousStats.averageTicket,
                    if (currentStats.totalItems > 0) currentStats.totalSales / currentStats.totalItems else 0.0
                )
            )
        )
    }

    override fun getRealtimeSalesStatistics(
        period: StatisticsPeriod,
        startDate: Date?,
        endDate: Date?,
    ): Flow<SalesStatistics> {
        return saleDao.getRealtimeSales()
            .map { _ -> getSalesStatistics(period, startDate, endDate) }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getTopSellingProducts(
        limit: Int,
        startDate: Date?,
        endDate: Date?,
    ): List<ProductSalesStats> {
        return saleDao.getTopSellingProducts(
            startDate ?: getDefaultStartDate(),
            endDate ?: Date(),
            limit
        ).map { stats ->
            ProductSalesStats(
                productId = stats.productId,
                productName = stats.productName,
                quantitySold = stats.quantitySold,
                totalRevenue = stats.totalRevenue,
                averagePrice = stats.averagePrice,
                profit = stats.profit,
                profitMargin = (stats.profit / stats.totalRevenue * 100).roundToInt() / 100.0
            )
        }
    }

    override suspend fun getProductStatistics(
        productId: Long,
        period: StatisticsPeriod,
        startDate: Date?,
        endDate: Date?,
    ): ProductSalesStats {
        val (start, end) = calculateDateRange(period, startDate, endDate)
        return saleDao.getProductStatistics(productId, start, end)?.let { stats ->
            ProductSalesStats(
                productId = stats.productId,
                productName = stats.productName,
                quantitySold = stats.quantitySold,
                totalRevenue = stats.totalRevenue,
                averagePrice = stats.averagePrice,
                profit = stats.profit,
                profitMargin = (stats.profit / stats.totalRevenue * 100).roundToInt() / 100.0
            )
        } ?: ProductSalesStats(
            productId = productId,
            productName = "",
            quantitySold = 0,
            totalRevenue = 0.0,
            averagePrice = 0.0,
            profit = 0.0,
            profitMargin = 0.0
        )
    }

    override suspend fun getSalesByPaymentMethod(
        startDate: Date?,
        endDate: Date?,
    ): Map<String, Double> {
        return saleDao.getSalesByPaymentMethod(
            startDate ?: getDefaultStartDate(),
            endDate ?: Date()
        )
    }

    override suspend fun getSalesByHourDistribution(
        startDate: Date?,
        endDate: Date?,
    ): Map<Int, Double> {
        return saleDao.getSalesByHour(
            startDate ?: getDefaultStartDate(),
            endDate ?: Date()
        )
    }

    override suspend fun generateSalesReport(
        period: StatisticsPeriod,
        startDate: Date?,
        endDate: Date?,
        format: ReportFormat,
    ): String {
        val stats = getSalesStatistics(period, startDate, endDate)
        val reportFile = when (format) {
            ReportFormat.PDF -> generatePdfReport(stats)
            ReportFormat.EXCEL -> generateExcelReport(stats)
        }
        return reportFile.absolutePath
    }

    override suspend fun exportSalesData(
        startDate: Date?,
        endDate: Date?,
        format: ExportFormat,
    ): String {
        val sales = saleDao.getSalesForExport(
            startDate ?: getDefaultStartDate(),
            endDate ?: Date()
        )
        val exportFile = when (format) {
            ExportFormat.CSV -> exportToCsv(sales)
            ExportFormat.EXCEL -> exportToExcel(sales)
            ExportFormat.JSON -> exportToJson(sales)
        }
        return exportFile.absolutePath
    }

    private suspend fun calculatePeriodStats(start: Date, end: Date): PeriodStats {
        val stats = saleDao.getPeriodStatistics(start, end)
        return PeriodStats(
            totalSales = stats.totalSales,
            totalItems = stats.totalItems,
            totalProfit = stats.totalProfit,
            profitMargin = if (stats.totalSales > 0) {
                (stats.totalProfit / stats.totalSales * 100).roundToInt() / 100.0
            } else 0.0,
            averageTicket = if (stats.totalItems > 0) {
                stats.totalSales / stats.totalItems
            } else 0.0
        )
    }

    private fun calculateDateRange(
        period: StatisticsPeriod,
        startDate: Date?,
        endDate: Date?,
    ): Pair<Date, Date> {
        if (startDate != null && endDate != null) {
            return Pair(startDate, endDate)
        }

        val end = endDate ?: Date()
        val start = when (period) {
            StatisticsPeriod.DAILY -> Calendar.getInstance().apply {
                time = end
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            StatisticsPeriod.WEEKLY -> Calendar.getInstance().apply {
                time = end
                add(Calendar.DAY_OF_YEAR, -7)
            }.time

            StatisticsPeriod.MONTHLY -> Calendar.getInstance().apply {
                time = end
                add(Calendar.MONTH, -1)
            }.time

            StatisticsPeriod.YEARLY -> Calendar.getInstance().apply {
                time = end
                add(Calendar.YEAR, -1)
            }.time

            StatisticsPeriod.CUSTOM -> startDate ?: getDefaultStartDate()
        }
        return Pair(start, end)
    }

    private fun calculatePreviousPeriodStart(period: StatisticsPeriod, start: Date): Date {
        return Calendar.getInstance().apply {
            time = start
            when (period) {
                StatisticsPeriod.DAILY -> add(Calendar.DAY_OF_YEAR, -1)
                StatisticsPeriod.WEEKLY -> add(Calendar.WEEK_OF_YEAR, -1)
                StatisticsPeriod.MONTHLY -> add(Calendar.MONTH, -1)
                StatisticsPeriod.YEARLY -> add(Calendar.YEAR, -1)
                StatisticsPeriod.CUSTOM -> add(Calendar.DAY_OF_YEAR, -7)
            }
        }.time
    }

    private fun calculateGrowth(previous: Double, current: Double): Double {
        if (previous == 0.0) return if (current > 0) 100.0 else 0.0
        return ((current - previous) / previous * 100).roundToInt() / 100.0
    }

    private fun getDefaultStartDate(): Date {
        return Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }.time
    }

    private data class PeriodStats(
        val totalSales: Double,
        val totalItems: Int,
        val totalProfit: Double,
        val profitMargin: Double,
        val averageTicket: Double,
    )

    private fun generatePdfReport(stats: SalesStatistics): File {
        // TODO: Implement PDF generation using a PDF library
        throw NotImplementedError("PDF generation not implemented yet")
    }

    private fun generateExcelReport(stats: SalesStatistics): File {
        // TODO: Implement Excel generation using Apache POI
        throw NotImplementedError("Excel generation not implemented yet")
    }

    private fun exportToCsv(sales: List<Any>): File {
        // TODO: Implement CSV export
        throw NotImplementedError("CSV export not implemented yet")
    }

    private fun exportToExcel(sales: List<Any>): File {
        // TODO: Implement Excel export
        throw NotImplementedError("Excel export not implemented yet")
    }

    private fun exportToJson(sales: List<Any>): File {
        // TODO: Implement JSON export
        throw NotImplementedError("JSON export not implemented yet")
    }
} 
