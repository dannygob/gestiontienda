package com.gestiontienda.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gestiontienda.android.domain.model.Permission
import com.gestiontienda.android.domain.model.UserRole
import java.util.Date

@Entity(tableName = "user_roles")
data class UserRoleEntity(
    @PrimaryKey
    val userId: String,  // Firebase Auth UID
    val role: UserRole,
    val permissions: Set<Permission>,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
) 