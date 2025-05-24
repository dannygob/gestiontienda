package com.gestiontienda.android.data.local.dao

import androidx.room.*
import com.gestiontienda.android.data.local.entities.LoyaltyConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoyaltyConfigDao {
    @Query("SELECT * FROM loyalty_config WHERE id = 1")
    fun getLoyaltyConfig(): Flow<LoyaltyConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: LoyaltyConfigEntity)

    @Update
    suspend fun updateConfig(config: LoyaltyConfigEntity)

    @Query(
        """
        SELECT CAST((:amount * pointsPerCurrency) AS INTEGER)
        FROM loyalty_config
        WHERE id = 1
    """
    )
    suspend fun calculatePointsForAmount(amount: Double): Int

    @Query(
        """
        SELECT CAST(((:points / redemptionRate) * 100) AS INTEGER) / 100.0
        FROM loyalty_config
        WHERE id = 1
    """
    )
    suspend fun calculateRedemptionValue(points: Int): Double
} 