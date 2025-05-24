package com.gestiontienda.android.domain.model

enum class UserRole {
    ADMIN,
    MANAGER,
    CASHIER,
    INVENTORY
}

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

data class UserPermission(
    val role: UserRole,
    val permissions: Set<Permission>,
) 