package com.gestiontienda.android.domain.model

import java.util.Date

data class StoreConfig(
    val storeName: String,
    val storeAddress: String? = null,
    val storePhone: String? = null,
    val storeEmail: String? = null,
    val taxRate: Double = 0.16,
    val currency: String = "MXN",
    val receiptHeader: String? = null,
    val receiptFooter: String? = null,
    val logoUrl: String? = null,
    val timezone: String = "America/Mexico_City",
    val updatedAt: Date = Date(),
)

data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val permissions: Set<Permission>,
    val isEmailVerified: Boolean,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
)

data class UserWithRole(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val isEmailVerified: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
) 
