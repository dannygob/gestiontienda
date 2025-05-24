package com.gestiontienda.android.domain.service

import com.gestiontienda.android.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface CustomerService {
    // Customer management
    fun getAllCustomers(): Flow<List<Customer>>
    fun getCustomersByStatus(status: CustomerStatus): Flow<List<Customer>>
    suspend fun getCustomerById(customerId: Long): Customer?
    fun searchCustomers(query: String): Flow<List<Customer>>
    suspend fun createCustomer(customer: Customer): Long
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)

    // Credit management
    fun getCustomerCredits(customerId: Long): Flow<List<CustomerCredit>>
    fun getOverdueCredits(): Flow<List<CustomerCredit>>
    suspend fun createCredit(credit: CustomerCredit): Long
    suspend fun updateCredit(credit: CustomerCredit)
    suspend fun deleteCredit(credit: CustomerCredit)
    suspend fun processPayment(creditId: Long, amount: Double)
    suspend fun increaseCreditUsed(customerId: Long, amount: Double)
    suspend fun decreaseCreditUsed(customerId: Long, amount: Double)

    // Loyalty program
    fun getLoyaltyConfig(): Flow<LoyaltyConfig>
    suspend fun updateLoyaltyConfig(config: LoyaltyConfig)
    suspend fun addLoyaltyPoints(customerId: Long, purchaseAmount: Double)
    suspend fun redeemLoyaltyPoints(customerId: Long, points: Int): Double
    fun getCustomersWithLoyaltyPoints(minPoints: Int): Flow<List<Customer>>

    // Purchase tracking
    suspend fun recordPurchase(
        customerId: Long,
        amount: Double,
        creditUsed: Double = 0.0,
        date: Date = Date(),
    )

    // Statistics
    fun getCustomerStatistics(customerId: Long): Flow<CustomerStatistics?>
    fun getTopCustomers(limit: Int = 10): Flow<List<CustomerWithStats>>
}

data class CustomerStatistics(
    val totalPurchases: Double,
    val purchaseCount: Int,
    val averagePurchase: Double,
    val totalLoyaltyPoints: Int,
    val totalCreditUsed: Double,
    val currentCredit: Double,
    val lastPurchaseDate: Date?,
)

data class CustomerWithStats(
    val customer: Customer,
    val statistics: CustomerStatistics,
) 
