package com.gestiontienda.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "loyalty_config")
data class LoyaltyConfigEntity(
    @PrimaryKey
    val id: Int = 1,  // Single row configuration
    val pointsPerCurrency: Double,  // Points earned per currency unit spent
    val minimumForRedemption: Int,  // Minimum points needed for redemption
    val redemptionRate: Double,     // Value of each point when redeeming
    val expirationMonths: Int,      // Number of months before points expire
    val updatedAt: Date = Date(),
) 