package com.gestiontienda.android.domain.repository

import com.gestiontienda.android.domain.model.StoreConfig
import com.gestiontienda.android.domain.model.User
import com.gestiontienda.android.domain.model.UserWithRole
import com.gestiontienda.android.data.local.entities.UserRole
import com.gestiontienda.android.data.local.entities.Permission
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    // Store configuration
    fun getStoreConfig(): Flow<StoreConfig?>
    suspend fun updateStoreConfig(config: StoreConfig)

    // User roles and permissions
    fun getCurrentUserRole(): Flow<UserRole?>
    fun getCurrentUserPermissions(): Flow<Set<Permission>>
    fun getAllUsers(): Flow<List<UserWithRole>>
    fun getUsersByRole(role: UserRole): Flow<List<UserWithRole>>
    suspend fun updateUserRole(userId: String, role: UserRole)
    suspend fun deleteUserRole(userId: String)

    // Permission checks
    fun hasPermission(permission: Permission): Flow<Boolean>
    fun hasAnyPermission(permissions: Set<Permission>): Flow<Boolean>
    fun hasAllPermissions(permissions: Set<Permission>): Flow<Boolean>
} 
