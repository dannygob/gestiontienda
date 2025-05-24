package com.gestiontienda.android.data.local.dao

import androidx.room.*
import com.gestiontienda.android.data.local.entities.CustomerCreditEntity
import com.gestiontienda.android.domain.model.CreditStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CustomerCreditDao {
    @Query("SELECT * FROM customer_credits WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCustomerCredits(customerId: Long): Flow<List<CustomerCreditEntity>>

    @Query("SELECT * FROM customer_credits WHERE status = :status ORDER BY dueDate ASC")
    fun getCreditsByStatus(status: CreditStatus): Flow<List<CustomerCreditEntity>>

    @Query("SELECT * FROM customer_credits WHERE dueDate <= :date AND status = :status")
    fun getOverdueCredits(
        date: Date = Date(),
        status: CreditStatus = CreditStatus.ACTIVE,
    ): Flow<List<CustomerCreditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredit(credit: CustomerCreditEntity): Long

    @Update
    suspend fun updateCredit(credit: CustomerCreditEntity)

    @Delete
    suspend fun deleteCredit(credit: CustomerCreditEntity)

    @Query("UPDATE customer_credits SET status = :newStatus WHERE id = :creditId")
    suspend fun updateCreditStatus(creditId: Long, newStatus: CreditStatus)

    @Query("UPDATE customer_credits SET remainingAmount = :remainingAmount WHERE id = :creditId")
    suspend fun updateRemainingAmount(creditId: Long, remainingAmount: Double)

    @Query(
        """
        SELECT * FROM customer_credits 
        WHERE customerId = :customerId 
        AND status = :status 
        ORDER BY createdAt DESC 
        LIMIT 1
    """
    )
    suspend fun getLatestCustomerCredit(
        customerId: Long,
        status: CreditStatus = CreditStatus.ACTIVE,
    ): CustomerCreditEntity?

    @Query("SELECT SUM(remainingAmount) FROM customer_credits WHERE customerId = :customerId AND status = :status")
    suspend fun getTotalCustomerCredit(
        customerId: Long,
        status: CreditStatus = CreditStatus.ACTIVE,
    ): Double?
} 