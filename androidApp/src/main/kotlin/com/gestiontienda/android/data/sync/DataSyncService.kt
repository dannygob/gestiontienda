package com.gestiontienda.android.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.gestiontienda.android.data.local.dao.ProductDao
import com.gestiontienda.android.data.local.dao.SaleDao
import com.gestiontienda.android.data.local.entities.ProductEntity
import com.gestiontienda.android.data.local.entities.SaleEntity
import com.gestiontienda.android.data.local.entities.SaleItemEntity
import com.gestiontienda.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
) {
    private val userId: String?
        get() = authRepository.currentUser?.id

    suspend fun syncToCloud() {
        userId?.let { uid ->
            // Sync products
            val products = productDao.getAllProducts().first()
            val productsCollection = firestore.collection("users/$uid/products")
            products.forEach { product ->
                productsCollection.document(product.id.toString())
                    .set(product)
                    .await()
            }

            // Sync sales
            val sales = saleDao.getAllSales().first()
            val salesCollection = firestore.collection("users/$uid/sales")
            sales.forEach { saleWithItems ->
                val sale = saleWithItems.sale
                val items = saleWithItems.items.map { it.saleItem }

                // Create a batch write
                firestore.runBatch { batch ->
                    // Save sale
                    val saleRef = salesCollection.document(sale.id.toString())
                    batch.set(saleRef, sale)

                    // Save sale items
                    val itemsCollection = saleRef.collection("items")
                    items.forEach { item ->
                        batch.set(itemsCollection.document(item.id.toString()), item)
                    }
                }.await()
            }
        }
    }

    suspend fun syncFromCloud() {
        userId?.let { uid ->
            // Sync products
            val productsCollection = firestore.collection("users/$uid/products")
            val productsSnapshot = productsCollection.get().await()
            val products = productsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(ProductEntity::class.java)
            }
            products.forEach { product ->
                productDao.insertProduct(product)
            }

            // Sync sales
            val salesCollection = firestore.collection("users/$uid/sales")
            val salesSnapshot = salesCollection.get().await()

            salesSnapshot.documents.forEach { saleDoc ->
                val sale = saleDoc.toObject(SaleEntity::class.java)
                if (sale != null) {
                    // Get sale items
                    val itemsSnapshot = saleDoc.reference.collection("items").get().await()
                    val items = itemsSnapshot.documents.mapNotNull { itemDoc ->
                        itemDoc.toObject(SaleItemEntity::class.java)
                    }

                    // Insert sale and items
                    saleDao.createSaleWithItems(sale, items)
                }
            }
        }
    }

    suspend fun performInitialSync() {
        // First try to sync from cloud
        syncFromCloud()
        // Then sync local changes back to cloud
        syncToCloud()
    }
} 
