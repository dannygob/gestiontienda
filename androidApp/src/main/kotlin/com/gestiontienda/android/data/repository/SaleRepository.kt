package com.gestiontienda.android.data.repository

import com.gestiontienda.android.data.local.dao.ProductDao
import com.gestiontienda.android.data.local.dao.SaleDao
import com.gestiontienda.android.data.local.entities.*
import kotlinx.coroutines.flow.*
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
) {
    fun getAllSales() = saleDao.getAllSales()

    fun getSalesByDateRange(startDate: Date, endDate: Date) =
        saleDao.getSalesByDateRange(startDate, endDate)

    suspend fun getSaleById(saleId: Long) = saleDao.getSaleById(saleId)

    suspend fun createSale(
        items: List<CartItem>,
        paymentMethod: PaymentMethod,
        notes: String? = null,
    ): Result<Long> {
        return try {
            // Calculate total
            val total = items.sumOf { it.quantity * it.product.price }

            // Create sale entity
            val sale = SaleEntity(
                total = total,
                paymentMethod = paymentMethod.name,
                notes = notes
            )

            // Create sale items
            val saleItems = items.map { cartItem ->
                SaleItemEntity(
                    saleId = 0, // Will be updated by Room
                    productId = cartItem.product.id,
                    quantity = cartItem.quantity,
                    priceAtSale = cartItem.product.price
                )
            }

            // Update product stock
            items.forEach { cartItem ->
                val product = cartItem.product
                val newStock = product.stock - cartItem.quantity
                productDao.updateProduct(product.copy(stock = newStock))
            }

            // Create sale with items
            val saleId = saleDao.insertSaleWithItems(sale, saleItems)
            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSaleStatus(saleId: Long, status: SaleStatus) {
        try {
            saleDao.updateSaleStatus(saleId, status.name)

            // If cancelling or refunding, restore stock
            if (status == SaleStatus.CANCELLED || status == SaleStatus.REFUNDED) {
                val sale = saleDao.getSaleById(saleId)
                sale?.items?.forEach { saleItemWithProduct ->
                    val product = saleItemWithProduct.product
                    val quantity = saleItemWithProduct.saleItem.quantity
                    productDao.updateProduct(
                        product.copy(stock = product.stock + quantity)
                    )
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getSalesStats(startDate: Date, endDate: Date) =
        saleDao.getSalesStats(startDate, endDate)

    suspend fun getTopSellingProducts(
        startDate: Date,
        endDate: Date,
        limit: Int = 10,
    ) = saleDao.getTopSellingProducts(startDate, endDate, limit)

    suspend fun getDailySalesStats(startDate: Date, endDate: Date) =
        saleDao.getDailySalesStats(startDate, endDate)
}

data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
) 
