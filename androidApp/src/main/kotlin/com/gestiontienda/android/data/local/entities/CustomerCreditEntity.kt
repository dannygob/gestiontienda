package com.gestiontienda.android.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gestiontienda.android.domain.model.CreditStatus
import com.gestiontienda.android.domain.model.CustomerCredit
import java.util.Date

@Entity(
    tableName = "customer_credits",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class CustomerCreditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val remainingAmount: Double,
    val dueDate: Date,
    val status: CreditStatus,
    val notes: String?,
    val createdAt: Date = Date(),
) {
    fun toCustomerCredit() = CustomerCredit(
        id = id,
        customerId = customerId,
        amount = amount,
        remainingAmount = remainingAmount,
        dueDate = dueDate,
        status = status,
        notes = notes,
        createdAt = createdAt
    )

    companion object {
        fun fromCustomerCredit(credit: CustomerCredit) = CustomerCreditEntity(
            id = credit.id,
            customerId = credit.customerId,
            amount = credit.amount,
            remainingAmount = credit.remainingAmount,
            dueDate = credit.dueDate,
            status = credit.status,
            notes = credit.notes,
            createdAt = credit.createdAt
        )
    }
} 