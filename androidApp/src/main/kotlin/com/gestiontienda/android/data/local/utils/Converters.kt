package com.gestiontienda.android.data.local.utils

import androidx.room.TypeConverter
import com.gestiontienda.android.domain.model.Permission
import com.gestiontienda.android.domain.model.UserRole
import com.gestiontienda.android.domain.model.CustomerStatus
import com.gestiontienda.android.domain.model.CreditStatus
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromUserRole(value: UserRole?): String? {
        return value?.name
    }

    @TypeConverter
    fun toUserRole(value: String?): UserRole? {
        return value?.let { UserRole.valueOf(it) }
    }

    @TypeConverter
    fun fromCustomerStatus(value: CustomerStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toCustomerStatus(value: String?): CustomerStatus? {
        return value?.let { CustomerStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromCreditStatus(status: CreditStatus): String {
        return status.name
    }

    @TypeConverter
    fun toCreditStatus(value: String): CreditStatus {
        return CreditStatus.valueOf(value)
    }

    @TypeConverter
    fun fromPermissionSet(permissions: Set<Permission>): String {
        return permissions.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toPermissionSet(value: String): Set<Permission> {
        if (value.isBlank()) return emptySet()
        return value.split(",").map { Permission.valueOf(it.trim()) }.toSet()
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, Double>): String {
        return value.entries.joinToString(separator = ",") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, Double> {
        if (value.isBlank()) return emptyMap()
        return value.split(",").associate {
            val (key, amount) = it.split(":")
            key to amount.toDouble()
        }
    }

    @TypeConverter
    fun fromIntMap(value: Map<Int, Double>): String {
        return value.entries.joinToString(separator = ",") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toIntMap(value: String): Map<Int, Double> {
        if (value.isBlank()) return emptyMap()
        return value.split(",").associate {
            val (key, amount) = it.split(":")
            key.toInt() to amount.toDouble()
        }
    }
} 