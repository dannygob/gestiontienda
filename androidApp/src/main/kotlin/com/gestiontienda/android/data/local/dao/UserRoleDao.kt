package com.gestiontienda.android.data.local.dao

import androidx.room.*
import com.gestiontienda.android.data.local.entities.UserRoleEntity
import com.gestiontienda.android.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRoleDao {
    @Query("SELECT * FROM user_roles ORDER BY role ASC")
    fun getAllUserRoles(): Flow<List<UserRoleEntity>>

    @Query("SELECT * FROM user_roles WHERE userId = :userId")
    suspend fun getUserRole(userId: String): UserRoleEntity?

    @Query("SELECT * FROM user_roles WHERE role = :role")
    fun getUsersByRole(role: UserRole): Flow<List<UserRoleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRole(userRole: UserRoleEntity)

    @Update
    suspend fun updateUserRole(userRole: UserRoleEntity)

    @Delete
    suspend fun deleteUserRole(userRole: UserRoleEntity)

    @Query("DELETE FROM user_roles WHERE userId = :userId")
    suspend fun deleteUserRoleByUserId(userId: String)
} 