package com.gestiontienda.android.data.local.entities

import androidx.room.DatabaseView

@DatabaseView(
    viewName = "product_sales_stats",
    value = """
    SELECT 
        p.id as productId,
        p.name as productName,
        SUM(si.quantity) as quantitySold,
        SUM(si.priceAtSale * si.quantity) as totalRevenue,
        AVG(si.priceAtSale) as averagePrice,
        SUM((si.priceAtSale - p.purchasePrice) * si.quantity) as profit,
        (SUM((si.priceAtSale - p.purchasePrice) * si.quantity) / NULLIF(SUM(si.priceAtSale * si.quantity), 0)) * 100 as profitMargin
    FROM sale_items si
    JOIN products p ON p.id = si.productId
    JOIN sales s ON s.id = si.saleId
    GROUP BY p.id, p.name
    """
)
data class ProductSalesStatsEntity(
    val productId: Long,
    val productName: String,
    val quantitySold: Int,
    val totalRevenue: Double,
    val averagePrice: Double,
    val profit: Double,
    val profitMargin: Double,
)

@DatabaseView(
    viewName = "period_stats",
    value = """
    SELECT 
        COALESCE(SUM(s.total), 0) as totalSales,
        COUNT(DISTINCT s.id) as totalTransactions,
        COALESCE(SUM((si.priceAtSale - p.purchasePrice) * si.quantity), 0) as totalProfit,
        COALESCE(SUM(si.quantity), 0) as totalItems
    FROM sales s
    LEFT JOIN sale_items si ON s.id = si.saleId
    LEFT JOIN products p ON si.productId = p.id
    """
)
data class PeriodStatsEntity(
    val totalSales: Double,
    val totalTransactions: Int,
    val totalProfit: Double,
    val totalItems: Int,
) 