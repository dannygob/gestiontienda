package com.gestiontienda.android.data.service

import com.gestiontienda.android.data.local.dao.CustomerDao
import com.gestiontienda.android.data.local.dao.CustomerCreditDao
import com.gestiontienda.android.data.local.dao.LoyaltyConfigDao
import com.gestiontienda.android.data.local.entities.CustomerCreditEntity
import com.gestiontienda.android.data.local.entities.CustomerEntity
import com.gestiontienda.android.data.local.entities.LoyaltyConfigEntity
import com.gestiontienda.android.domain.model.Customer
import com.gestiontienda.android.domain.model.CustomerCredit
import com.gestiontienda.android.domain.model.CustomerStatistics
import com.gestiontienda.android.domain.model.CustomerWithStats
import com.gestiontienda.android.domain.model.CreditStatus
import com.gestiontienda.android.domain.model.CustomerStatus
import com.gestiontienda.android.domain.model.LoyaltyConfig
import com.gestiontienda.android.domain.service.CustomerService
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

@Singleton
class CustomerServiceImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val creditDao: CustomerCreditDao,
    private val loyaltyConfigDao: LoyaltyConfigDao,
) : CustomerService {

    override fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { it.toCustomer() }
        }
    }

    override fun getCustomersByStatus(status: CustomerStatus): Flow<List<Customer>> {
        return customerDao.getCustomersByStatus(status).map { entities ->
            entities.map { it.toCustomer() }
        }
    }

    override suspend fun getCustomerById(customerId: Long): Customer? {
        return customerDao.getCustomerById(customerId)
            .map { it?.toCustomer() }
            .firstOrNull()
    }

    override fun searchCustomers(query: String): Flow<List<Customer>> {
        return customerDao.searchCustomers(query).map { entities ->
            entities.map { it.toCustomer() }
        }
    }

    override suspend fun createCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(CustomerEntity.fromCustomer(customer))
    }

    override suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(CustomerEntity.fromCustomer(customer))
    }

    override suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(CustomerEntity.fromCustomer(customer))
    }

    override fun getCustomerCredits(customerId: Long): Flow<List<CustomerCredit>> {
        return creditDao.getCustomerCredits(customerId).map { entities ->
            entities.map { it.toCustomerCredit() }
        }
    }

    override fun getOverdueCredits(): Flow<List<CustomerCredit>> {
        return creditDao.getOverdueCredits().map { entities ->
            entities.map { it.toCustomerCredit() }
        }
    }

    override suspend fun createCredit(credit: CustomerCredit): Long {
        val creditId = creditDao.insertCredit(CustomerCreditEntity.fromCustomerCredit(credit))
        customerDao.increaseCreditUsed(credit.customerId, credit.amount)
        return creditId
    }

    override suspend fun updateCredit(credit: CustomerCredit) {
        creditDao.updateCredit(CustomerCreditEntity.fromCustomerCredit(credit))
    }

    override suspend fun deleteCredit(credit: CustomerCredit) {
        creditDao.deleteCredit(CustomerCreditEntity.fromCustomerCredit(credit))
        if (credit.status == CreditStatus.ACTIVE) {
            customerDao.decreaseCreditUsed(credit.customerId, credit.remainingAmount)
        }
    }

    override suspend fun processPayment(creditId: Long, amount: Double) {
        val credit = creditDao.getCreditById(creditId) ?: return
        creditDao.updateCreditPayment(creditId, amount)
        customerDao.decreaseCreditUsed(credit.customerId, amount)
    }

    override suspend fun increaseCreditUsed(customerId: Long, amount: Double) {
        customerDao.increaseCreditUsed(customerId, amount)
    }

    override suspend fun decreaseCreditUsed(customerId: Long, amount: Double) {
        customerDao.decreaseCreditUsed(customerId, amount)
    }

    override fun getCustomersWithLoyaltyPoints(minPoints: Int): Flow<List<Customer>> {
        return customerDao.getCustomersWithLoyaltyPoints(minPoints)
            .map { entities ->
                entities.map { it.toCustomer() }
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun recordPurchase(
        customerId: Long,
        amount: Double,
        creditUsed: Double,
        date: Date,
    ) {
        customerDao.updateCustomerPurchaseStats(customerId, amount, date)
        if (creditUsed > 0) {
            customerDao.increaseCreditUsed(customerId, creditUsed)
        }
        addLoyaltyPoints(customerId, amount - creditUsed) // Only earn points on non-credit portion
    }

    override fun getCustomerStatistics(customerId: Long): Flow<CustomerStatistics?> = flow {
        try {
            customerDao.getCustomerById(customerId)
                .collect { entity ->
                    emit(entity?.let {
                        CustomerStatistics(
                            totalPurchases = it.totalPurchases,
                            purchaseCount = it.purchaseCount,
                            averagePurchase = if (it.purchaseCount > 0) it.totalPurchases / it.purchaseCount else 0.0,
                            totalLoyaltyPoints = it.loyaltyPoints,
                            totalCreditUsed = it.currentCredit,
                            currentCredit = it.currentCredit,
                            lastPurchaseDate = it.lastPurchaseDate
                        )
                    })
                }
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    override fun getTopCustomers(limit: Int): Flow<List<CustomerWithStats>> = flow {
        try {
            customerDao.getAllCustomers()
                .collect { entities ->
                    emit(entities.map { entity ->
                        CustomerWithStats(
                            customer = entity.toCustomer(),
                            statistics = CustomerStatistics(
                                totalPurchases = entity.totalPurchases,
                                purchaseCount = entity.purchaseCount,
                                averagePurchase = if (entity.purchaseCount > 0) entity.totalPurchases / entity.purchaseCount else 0.0,
                                totalLoyaltyPoints = entity.loyaltyPoints,
                                totalCreditUsed = entity.currentCredit,
                                currentCredit = entity.currentCredit,
                                lastPurchaseDate = entity.lastPurchaseDate
                            )
                        )
                    }
                        .sortedByDescending { it.statistics.totalPurchases }
                        .take(limit))
                }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override fun getLoyaltyConfig(): Flow<LoyaltyConfig> {
        return loyaltyConfigDao.getLoyaltyConfig()
            .map { entity ->
                entity?.let {
                    LoyaltyConfig(
                        pointsPerCurrency = it.pointsPerCurrency,
                        minimumForRedemption = it.minimumForRedemption,
                        redemptionRate = it.redemptionRate,
                        expirationMonths = it.expirationMonths
                    )
                } ?: LoyaltyConfig()
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun updateLoyaltyConfig(config: LoyaltyConfig) {
        loyaltyConfigDao.updateConfig(
            LoyaltyConfigEntity(
                pointsPerCurrency = config.pointsPerCurrency,
                minimumForRedemption = config.minimumForRedemption,
                redemptionRate = config.redemptionRate,
                expirationMonths = config.expirationMonths
            )
        )
    }

    override suspend fun addLoyaltyPoints(customerId: Long, purchaseAmount: Double) {
        val config = loyaltyConfigDao.getLoyaltyConfig()
            .map {
                it ?: LoyaltyConfigEntity(
                    pointsPerCurrency = 1.0,
                    minimumForRedemption = 100,
                    redemptionRate = 0.01,
                    expirationMonths = 12
                )
            }
            .first()
        val points = (purchaseAmount * config.pointsPerCurrency).toInt()
        if (points > 0) {
            customerDao.addLoyaltyPoints(customerId, points)
        }
    }

    override suspend fun redeemLoyaltyPoints(customerId: Long, points: Int): Double {
        val config = loyaltyConfigDao.getLoyaltyConfig()
            .map {
                it ?: LoyaltyConfigEntity(
                    pointsPerCurrency = 1.0,
                    minimumForRedemption = 100,
                    redemptionRate = 0.01,
                    expirationMonths = 12
                )
            }
            .first()
        if (points < config.minimumForRedemption) {
            throw IllegalArgumentException("Puntos insuficientes para canjear")
        }

        val customer = customerDao.getCustomerById(customerId)
            .map { it?.toCustomer() }
            .firstOrNull() ?: throw IllegalArgumentException("Cliente no encontrado")

        if (customer.loyaltyPoints < points) {
            throw IllegalArgumentException("Puntos insuficientes")
        }

        customerDao.redeemLoyaltyPoints(customerId, points)
        return points * config.redemptionRate
    }
} 
