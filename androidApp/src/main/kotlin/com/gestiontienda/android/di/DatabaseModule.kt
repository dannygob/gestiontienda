package com.gestiontienda.android.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gestiontienda.android.data.local.AppDatabase
import com.gestiontienda.android.data.local.dao.ProductDao
import com.gestiontienda.android.data.local.dao.SaleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Drop existing views
            database.execSQL("DROP VIEW IF EXISTS product_sales_stats")
            database.execSQL("DROP VIEW IF EXISTS period_stats")

            // Recreate views with updated definitions
            database.execSQL(
                """
                CREATE VIEW product_sales_stats AS
                SELECT 
                    p.id as productId,
                    p.name as productName,
                    SUM(si.quantity) as quantitySold,
                    SUM(si.priceAtSale * si.quantity) as totalRevenue,
                    AVG(si.priceAtSale) as averagePrice,
                    SUM((si.priceAtSale - p.purchasePrice) * si.quantity) as profit,
                    (SUM((si.priceAtSale - p.purchasePrice) * si.quantity) / NULLIF(SUM(si.priceAtSale * si.quantity), 0)) * 100 as profitMargin
                FROM sale_items si
                JOIN products p ON p.id = si.productId
                JOIN sales s ON s.id = si.saleId
                GROUP BY p.id, p.name
            """
            )

            database.execSQL(
                """
                CREATE VIEW period_stats AS
                SELECT 
                    COALESCE(SUM(s.total), 0) as totalSales,
                    COUNT(DISTINCT s.id) as totalTransactions,
                    COALESCE(SUM((si.priceAtSale - p.purchasePrice) * si.quantity), 0) as totalProfit,
                    COALESCE(SUM(si.quantity), 0) as totalItems
                FROM sales s
                LEFT JOIN sale_items si ON s.id = si.saleId
                LEFT JOIN products p ON si.productId = p.id
            """
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gestiontienda.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideSaleDao(database: AppDatabase): SaleDao {
        return database.saleDao()
    }
} 