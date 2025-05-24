package com.gestiontienda.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gestiontienda.android.data.local.dao.*
import com.gestiontienda.android.data.local.entities.*
import com.gestiontienda.android.data.local.utils.Converters

@Database(
    entities = [
        CustomerEntity::class,
        CustomerCreditEntity::class,
        LoyaltyConfigEntity::class,
        StoreConfigEntity::class,
        UserRoleEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        ProductEntity::class
    ],
    views = [
        ProductSalesStatsEntity::class,
        PeriodStatsEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun customerCreditDao(): CustomerCreditDao
    abstract fun loyaltyConfigDao(): LoyaltyConfigDao
    abstract fun storeConfigDao(): StoreConfigDao
    abstract fun userRoleDao(): UserRoleDao
    abstract fun saleDao(): SaleDao
    abstract fun productDao(): ProductDao
} 
