package com.gestiontienda.android.domain.model

data class LoyaltyConfig(
    val pointsPerCurrency: Double = 1.0,
    val minimumForRedemption: Int = 100,
    val redemptionRate: Double = 0.01,
    val expirationMonths: Int = 12,
) 