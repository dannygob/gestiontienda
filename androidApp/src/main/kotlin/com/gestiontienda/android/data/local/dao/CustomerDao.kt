package com.gestiontienda.android.data.local.dao

import androidx.room.*
import com.gestiontienda.android.data.local.entities.*
import com.gestiontienda.android.domain.model.CustomerStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE status = :status ORDER BY name ASC")
    fun getCustomersByStatus(status: CustomerStatus): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query(
        """
        SELECT * FROM customers 
        WHERE name LIKE :query 
        OR email LIKE :query 
        OR phone LIKE :query 
        ORDER BY 
            CASE 
                WHEN name LIKE :query || '%' THEN 1
                WHEN name LIKE '%' || :query || '%' THEN 2
                ELSE 3
            END,
            name ASC
        LIMIT 50
    """
    )
    suspend fun searchCustomers(query: String): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET loyaltyPoints = loyaltyPoints + :points WHERE id = :customerId")
    suspend fun addLoyaltyPoints(customerId: Long, points: Int)

    @Query("UPDATE customers SET loyaltyPoints = loyaltyPoints - :points WHERE id = :customerId AND loyaltyPoints >= :points")
    suspend fun redeemLoyaltyPoints(customerId: Long, points: Int)

    @Query(
        """
        UPDATE customers 
        SET currentCredit = currentCredit + :amount,
            totalPurchases = totalPurchases + :amount,
            purchaseCount = purchaseCount + 1,
            lastPurchaseDate = :date
        WHERE id = :customerId
    """
    )
    suspend fun recordPurchase(customerId: Long, amount: Double, date: Date = Date())

    @Query("SELECT * FROM customers WHERE currentCredit > 0 ORDER BY currentCredit DESC")
    fun getCustomersWithCredit(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE status = 'ACTIVE' AND lastPurchaseDate < :date")
    fun getInactiveCustomers(date: Date): Flow<List<CustomerEntity>>

    @Query(
        """
        SELECT * FROM customers 
        WHERE loyaltyPoints >= :minPoints 
        AND status = 'ACTIVE'
        ORDER BY loyaltyPoints DESC
    """
    )
    fun getCustomersWithLoyaltyPoints(minPoints: Int): Flow<List<CustomerEntity>>

    @Query(
        """
        UPDATE customers 
        SET currentCredit = currentCredit + :amount,
        updatedAt = :date
        WHERE id = :customerId
    """
    )
    suspend fun increaseCreditUsed(customerId: Long, amount: Double, date: Date = Date())

    @Query(
        """
        UPDATE customers 
        SET currentCredit = currentCredit - :amount,
        updatedAt = :date
        WHERE id = :customerId
    """
    )
    suspend fun decreaseCreditUsed(customerId: Long, amount: Double, date: Date = Date())

    @Query(
        """
        UPDATE customers 
        SET lastPurchaseDate = :date,
        totalPurchases = totalPurchases + :amount,
        purchaseCount = purchaseCount + 1,
        updatedAt = :date
        WHERE id = :customerId
    """
    )
    suspend fun updateCustomerPurchaseStats(
        customerId: Long,
        amount: Double,
        date: Date = Date(),
    )
} 
