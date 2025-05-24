package com.gestiontienda.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "store_config")
data class StoreConfigEntity(
    @PrimaryKey
    val id: Int = 1,  // Single row configuration
    val storeName: String,
    val address: String?,
    val phone: String?,
    val email: String?,
    val taxId: String?,
    val currencyCode: String = "USD",
    val taxRate: Double = 0.0,
    val defaultCreditLimit: Double = 0.0,
    val updatedAt: Date = Date(),
)

@Entity(tableName = "user_roles")
data class UserRoleEntity(
    @PrimaryKey
    val userId: String, // Firebase Auth UID
    val role: UserRole,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
)

enum class UserRole {
    ADMIN,
    MANAGER,
    CASHIER,
    INVENTORY
}

data class UserPermission(
    val role: UserRole,
    val permissions: Set<Permission>,
)

enum class Permission {
    // Sales permissions
    MANAGE_SALES,
    VIEW_SALES,
    CANCEL_SALES,
    APPLY_DISCOUNTS,

    // Inventory permissions
    MANAGE_INVENTORY,
    VIEW_INVENTORY,
    ADJUST_STOCK,

    // Customer permissions
    MANAGE_CUSTOMERS,
    VIEW_CUSTOMERS,
    MANAGE_CREDITS,

    // Financial permissions
    VIEW_STATISTICS,
    EXPORT_REPORTS,
    MANAGE_PRICING,

    // System permissions
    MANAGE_USERS,
    MANAGE_SETTINGS,
    MANAGE_BACKUPS
} 
