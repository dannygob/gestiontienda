package com.gestiontienda.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseHelper(private val context: Context) {
    private var database: StoreDatabase? = null

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "store.db"
        )
    }

    actual fun getDatabase(): StoreDatabase {
        if (database == null) {
            database = StoreDatabase(createDriver())
        }
        return database!!
    }
} 
