package com.gestiontienda.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseHelper {
    private var database: StoreDatabase? = null

    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = Database.Schema,
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
