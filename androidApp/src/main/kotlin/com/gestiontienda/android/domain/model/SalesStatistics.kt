package com.gestiontienda.android.domain.model

import java.util.Date

data class SalesStatistics(
    val period: StatisticsPeriod,
    val totalSales: Double,
    val totalItems: Int,
    val averageTicket: Double,
    val topProducts: List<ProductSalesStats>,
    val salesByPaymentMethod: Map<String, Double>,
    val salesByHour: Map<Int, Double>,
    val startDate: Date,
    val endDate: Date,
    val profitMargin: Double,
    val totalProfit: Double,
    val comparisonWithPreviousPeriod: ComparisonStats,
)

data class ProductSalesStats(
    val productId: Long,
    val productName: String,
    val quantitySold: Int,
    val totalRevenue: Double,
    val averagePrice: Double,
    val profit: Double,
    val profitMargin: Double,
)

data class ComparisonStats(
    val salesGrowth: Double,
    val itemsGrowth: Double,
    val profitGrowth: Double,
    val averageTicketGrowth: Double,
)

enum class StatisticsPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM
} 
