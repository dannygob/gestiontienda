package com.gestiontienda.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

expect class DatabaseHelper {
    fun createDriver(): SqlDriver
    fun getDatabase(): StoreDatabase
}

class StoreDatabase(private val driver: SqlDriver) {
    private val database = Database(driver)

    // Product operations
    val productQueries get() = database.productQueries
    val proveedorQueries get() = database.proveedorQueries
    val clienteQueries get() = database.clienteQueries
    val personalQueries get() = database.personalQueries
    val ventaQueries get() = database.ventaQueries
    val detalleVentaQueries get() = database.detalleVentaQueries
    val compraQueries get() = database.compraQueries
    val detalleCompraQueries get() = database.detalleCompraQueries

    // Helper function to get current timestamp
    private fun getCurrentTimestamp(): Long = Clock.System.now().toEpochMilliseconds()

    // Product operations with Flow
    fun getAllProducts(): Flow<List<Product>> =
        productQueries.getAllProducts().asFlow().mapToList()

    fun getProductById(id: Long): Product? =
        productQueries.getProductById(id).executeAsOneOrNull()

    fun getProductByCode(code: String): Product? =
        productQueries.getProductByCode(code).executeAsOneOrNull()

    fun insertProduct(
        name: String,
        code: String,
        purchasePrice: Double,
        salePrice: Double,
        stock: Long,
        minStock: Long,
    ) {
        val now = getCurrentTimestamp()
        productQueries.insertProduct(
            name = name,
            code = code,
            purchase_price = purchasePrice,
            sale_price = salePrice,
            stock = stock,
            min_stock = minStock,
            created_at = now,
            updated_at = now
        )
    }

    fun updateProduct(
        id: Long,
        name: String,
        code: String,
        purchasePrice: Double,
        salePrice: Double,
        stock: Long,
        minStock: Long,
    ) {
        productQueries.updateProduct(
            name = name,
            code = code,
            purchase_price = purchasePrice,
            sale_price = salePrice,
            stock = stock,
            min_stock = minStock,
            updated_at = getCurrentTimestamp(),
            id = id
        )
    }

    fun updateProductStock(id: Long, stockChange: Long) {
        productQueries.updateProductStock(
            stockChange,
            getCurrentTimestamp(),
            id
        )
    }

    fun getLowStockProducts(): Flow<List<Product>> =
        productQueries.getLowStockProducts().asFlow().mapToList()

    // Proveedor operations
    fun getAllProveedores(): Flow<List<Proveedores>> =
        proveedorQueries.getAllProveedores().asFlow().mapToList()

    fun getProveedorById(id: Long): Proveedores? =
        proveedorQueries.getProveedorById(id).executeAsOneOrNull()

    fun insertProveedor(
        nombre: String,
        direccion: String?,
        telefono: String?,
        email: String?,
        rfc: String?,
    ) {
        val now = getCurrentTimestamp()
        proveedorQueries.insertProveedor(
            nombre = nombre,
            direccion = direccion,
            telefono = telefono,
            email = email,
            rfc = rfc,
            created_at = now,
            updated_at = now
        )
    }

    // Cliente operations
    fun getAllClientes(): Flow<List<Clientes>> =
        clienteQueries.getAllClientes().asFlow().mapToList()

    fun getClienteById(id: Long): Clientes? =
        clienteQueries.getClienteById(id).executeAsOneOrNull()

    fun insertCliente(
        nombre: String,
        direccion: String?,
        telefono: String?,
        email: String?,
        rfc: String?,
    ) {
        val now = getCurrentTimestamp()
        clienteQueries.insertCliente(
            nombre = nombre,
            direccion = direccion,
            telefono = telefono,
            email = email,
            rfc = rfc,
            created_at = now,
            updated_at = now
        )
    }

    // Personal operations
    fun getAllPersonal(): Flow<List<Personal>> =
        personalQueries.getAllPersonal().asFlow().mapToList()

    fun getPersonalById(id: Long): Personal? =
        personalQueries.getPersonalById(id).executeAsOneOrNull()

    fun insertPersonal(
        nombre: String,
        cargo: String,
        telefono: String?,
        email: String?,
        fechaIngreso: Long,
    ) {
        val now = getCurrentTimestamp()
        personalQueries.insertPersonal(
            nombre = nombre,
            cargo = cargo,
            telefono = telefono,
            email = email,
            fecha_ingreso = fechaIngreso,
            created_at = now,
            updated_at = now
        )
    }

    // Venta operations
    fun getAllVentas(): Flow<List<Ventas>> =
        ventaQueries.getAllVentas().asFlow().mapToList()

    fun getVentaById(id: Long): Ventas? =
        ventaQueries.getVentaById(id).executeAsOneOrNull()

    fun getVentasByDateRange(startDate: Long, endDate: Long): Flow<List<Ventas>> =
        ventaQueries.getVentasByDateRange(startDate, endDate).asFlow().mapToList()

    fun insertVenta(
        fecha: Long,
        clienteId: Long?,
        personalId: Long?,
        total: Double,
    ): Long {
        val now = getCurrentTimestamp()
        ventaQueries.insertVenta(
            fecha = fecha,
            cliente_id = clienteId,
            personal_id = personalId,
            total = total,
            created_at = now,
            updated_at = now
        )
        return ventaQueries.selectLastInsertedRowId().executeAsOne()
    }

    // DetalleVenta operations
    fun getDetallesByVentaId(ventaId: Long): Flow<List<DetalleVenta>> =
        detalleVentaQueries.getDetallesByVentaId(ventaId).asFlow().mapToList()

    fun insertDetalleVenta(
        ventaId: Long,
        productoId: Long,
        cantidad: Long,
        precioUnitario: Double,
        subtotal: Double,
    ) {
        val now = getCurrentTimestamp()
        detalleVentaQueries.insertDetalleVenta(
            venta_id = ventaId,
            producto_id = productoId,
            cantidad = cantidad,
            precio_unitario = precioUnitario,
            subtotal = subtotal,
            created_at = now,
            updated_at = now
        )
    }

    // Compra operations
    fun getAllCompras(): Flow<List<Compras>> =
        compraQueries.getAllCompras().asFlow().mapToList()

    fun getCompraById(id: Long): Compras? =
        compraQueries.getCompraById(id).executeAsOneOrNull()

    fun getComprasByDateRange(startDate: Long, endDate: Long): Flow<List<Compras>> =
        compraQueries.getComprasByDateRange(startDate, endDate).asFlow().mapToList()

    fun insertCompra(
        fecha: Long,
        proveedorId: Long?,
        personalId: Long?,
        total: Double,
    ): Long {
        val now = getCurrentTimestamp()
        compraQueries.insertCompra(
            fecha = fecha,
            proveedor_id = proveedorId,
            personal_id = personalId,
            total = total,
            created_at = now,
            updated_at = now
        )
        return compraQueries.selectLastInsertedRowId().executeAsOne()
    }

    // DetalleCompra operations
    fun getDetallesByCompraId(compraId: Long): Flow<List<DetalleCompra>> =
        detalleCompraQueries.getDetallesByCompraId(compraId).asFlow().mapToList()

    fun insertDetalleCompra(
        compraId: Long,
        productoId: Long,
        cantidad: Long,
        precioUnitario: Double,
        subtotal: Double,
    ) {
        val now = getCurrentTimestamp()
        detalleCompraQueries.insertDetalleCompra(
            compra_id = compraId,
            producto_id = productoId,
            cantidad = cantidad,
            precio_unitario = precioUnitario,
            subtotal = subtotal,
            created_at = now,
            updated_at = now
        )
    }
} 
