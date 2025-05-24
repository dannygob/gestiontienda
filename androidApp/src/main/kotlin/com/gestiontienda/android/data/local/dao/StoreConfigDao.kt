package com.gestiontienda.android.data.local.dao

import androidx.room.*
import com.gestiontienda.android.data.local.entities.StoreConfigEntity
import com.gestiontienda.android.data.local.entities.UserRole
import com.gestiontienda.android.data.local.entities.UserRoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreConfigDao {
    @Query("SELECT * FROM store_config WHERE id = 1")
    fun getStoreConfig(): Flow<StoreConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: StoreConfigEntity)

    @Update
    suspend fun updateConfig(config: StoreConfigEntity)

    @Query("UPDATE store_config SET taxRate = :rate WHERE id = 1")
    suspend fun updateTaxRate(rate: Double)

    @Query("UPDATE store_config SET defaultCreditLimit = :limit WHERE id = 1")
    suspend fun updateDefaultCreditLimit(limit: Double)
}

@Dao
interface UserRoleDao {
    @Query("SELECT * FROM user_roles WHERE userId = :userId")
    fun getUserRole(userId: String): Flow<UserRoleEntity?>

    @Query("SELECT * FROM user_roles")
    fun getAllUserRoles(): Flow<List<UserRoleEntity>>

    @Query("SELECT * FROM user_roles WHERE role = :role")
    fun getUsersByRole(role: UserRole): Flow<List<UserRoleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRole(userRole: UserRoleEntity)

    @Update
    suspend fun updateUserRole(userRole: UserRoleEntity)

    @Delete
    suspend fun deleteUserRole(userRole: UserRoleEntity)

    @Query("DELETE FROM user_roles WHERE userId = :userId")
    suspend fun deleteUserRoleById(userId: String)
} 