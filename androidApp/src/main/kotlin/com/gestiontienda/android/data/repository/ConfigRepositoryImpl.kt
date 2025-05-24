package com.gestiontienda.android.data.repository

import com.gestiontienda.android.data.local.dao.StoreConfigDao
import com.gestiontienda.android.data.local.dao.UserRoleDao
import com.gestiontienda.android.data.local.entities.*
import com.gestiontienda.android.domain.model.StoreConfig
import com.gestiontienda.android.domain.model.UserWithRole
import com.gestiontienda.android.domain.repository.AuthRepository
import com.gestiontienda.android.domain.repository.ConfigRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepositoryImpl @Inject constructor(
    private val storeConfigDao: StoreConfigDao,
    private val userRoleDao: UserRoleDao,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
) : ConfigRepository {

    private val defaultPermissions = mapOf(
        UserRole.ADMIN to Permission.values().toSet(),
        UserRole.MANAGER to setOf(
            Permission.MANAGE_SALES,
            Permission.VIEW_SALES,
            Permission.CANCEL_SALES,
            Permission.APPLY_DISCOUNTS,
            Permission.MANAGE_INVENTORY,
            Permission.VIEW_INVENTORY,
            Permission.ADJUST_STOCK,
            Permission.MANAGE_CUSTOMERS,
            Permission.VIEW_CUSTOMERS,
            Permission.MANAGE_CREDITS,
            Permission.VIEW_STATISTICS,
            Permission.EXPORT_REPORTS,
            Permission.MANAGE_PRICING
        ),
        UserRole.CASHIER to setOf(
            Permission.VIEW_SALES,
            Permission.APPLY_DISCOUNTS,
            Permission.VIEW_INVENTORY,
            Permission.VIEW_CUSTOMERS
        ),
        UserRole.INVENTORY to setOf(
            Permission.MANAGE_INVENTORY,
            Permission.VIEW_INVENTORY,
            Permission.ADJUST_STOCK,
            Permission.VIEW_STATISTICS
        )
    )

    override fun getStoreConfig(): Flow<StoreConfig?> {
        return storeConfigDao.getStoreConfig().map { entity ->
            entity?.let {
                StoreConfig(
                    storeName = it.storeName,
                    storeAddress = it.storeAddress,
                    storePhone = it.storePhone,
                    storeEmail = it.storeEmail,
                    taxRate = it.taxRate,
                    currency = it.currency,
                    receiptHeader = it.receiptHeader,
                    receiptFooter = it.receiptFooter,
                    logoUrl = it.logoUrl,
                    timezone = it.timezone,
                    updatedAt = it.updatedAt
                )
            }
        }
    }

    override suspend fun updateStoreConfig(config: StoreConfig) {
        val entity = StoreConfigEntity(
            storeName = config.storeName,
            storeAddress = config.storeAddress,
            storePhone = config.storePhone,
            storeEmail = config.storeEmail,
            taxRate = config.taxRate,
            currency = config.currency,
            receiptHeader = config.receiptHeader,
            receiptFooter = config.receiptFooter,
            logoUrl = config.logoUrl,
            timezone = config.timezone,
            updatedAt = Date()
        )
        storeConfigDao.insertConfig(entity)

        // Sync to Firestore
        val userId = authRepository.getCurrentUserEmail() ?: return
        firestore.collection("stores")
            .document(userId)
            .set(entity)
            .await()
    }

    override fun getCurrentUserRole(): Flow<UserRole?> {
        return authRepository.getCurrentUserEmail()?.let { userId ->
            userRoleDao.getUserRole(userId).map { it?.role }
        } ?: flowOf(null)
    }

    override fun getCurrentUserPermissions(): Flow<Set<Permission>> {
        return getCurrentUserRole().map { role ->
            role?.let { defaultPermissions[it] } ?: emptySet()
        }
    }

    override fun getAllUsers(): Flow<List<UserWithRole>> {
        return userRoleDao.getAllUserRoles().map { roles ->
            roles.map { role ->
                UserWithRole(
                    id = role.userId,
                    email = "", // Fetch from Firebase Auth
                    name = "", // Fetch from Firebase Auth
                    role = role.role,
                    isEmailVerified = false, // Fetch from Firebase Auth
                    createdAt = role.createdAt,
                    updatedAt = role.updatedAt
                )
            }
        }
    }

    override fun getUsersByRole(role: UserRole): Flow<List<UserWithRole>> {
        return userRoleDao.getUsersByRole(role).map { roles ->
            roles.map { userRole ->
                UserWithRole(
                    id = userRole.userId,
                    email = "", // Fetch from Firebase Auth
                    name = "", // Fetch from Firebase Auth
                    role = userRole.role,
                    isEmailVerified = false, // Fetch from Firebase Auth
                    createdAt = userRole.createdAt,
                    updatedAt = userRole.updatedAt
                )
            }
        }
    }

    override suspend fun updateUserRole(userId: String, role: UserRole) {
        val userRole = UserRoleEntity(
            userId = userId,
            role = role,
            updatedAt = Date()
        )
        userRoleDao.insertUserRole(userRole)

        // Sync to Firestore
        firestore.collection("user_roles")
            .document(userId)
            .set(userRole)
            .await()
    }

    override suspend fun deleteUserRole(userId: String) {
        userRoleDao.deleteUserRoleById(userId)

        // Delete from Firestore
        firestore.collection("user_roles")
            .document(userId)
            .delete()
            .await()
    }

    override fun hasPermission(permission: Permission): Flow<Boolean> {
        return getCurrentUserPermissions().map { permissions ->
            permissions.contains(permission)
        }
    }

    override fun hasAnyPermission(permissions: Set<Permission>): Flow<Boolean> {
        return getCurrentUserPermissions().map { userPermissions ->
            permissions.any { userPermissions.contains(it) }
        }
    }

    override fun hasAllPermissions(permissions: Set<Permission>): Flow<Boolean> {
        return getCurrentUserPermissions().map { userPermissions ->
            permissions.all { userPermissions.contains(it) }
        }
    }
} 
