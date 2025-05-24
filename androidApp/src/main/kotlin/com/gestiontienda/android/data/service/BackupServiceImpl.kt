package com.gestiontienda.android.data.service

import android.content.Context
import androidx.room.RoomDatabase
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.gestiontienda.android.data.local.AppDatabase
import com.gestiontienda.android.domain.service.BackupService
import com.gestiontienda.android.di.ServiceLocator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
) : BackupService {

    private val backupWorkName = "automatic_backup"

    override suspend fun backupToFirebase(): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")

        // Create temporary backup file
        val backupFile = createTempBackupFile()

        // Export database to file
        database.backupToFile(backupFile)

        // Upload to Firebase Storage
        val backupRef = storage.reference
            .child("backups")
            .child(userId)
            .child("backup_${System.currentTimeMillis()}.db")

        backupRef.putFile(backupFile.toUri()).await()

        // Clean up
        backupFile.delete()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun restoreFromFirebase(): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")

        // Get latest backup
        val backupRef = storage.reference
            .child("backups")
            .child(userId)
            .listAll()
            .await()
            .items
            .maxByOrNull { it.name }
            ?: throw IllegalStateException("No hay respaldos disponibles")

        // Create temporary file
        val tempFile = createTempBackupFile()

        // Download backup
        backupRef.getFile(tempFile).await()

        // Restore database
        database.restoreFromFile(tempFile)

        // Clean up
        tempFile.delete()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun createLocalBackup(file: File): Result<Unit> = try {
        database.backupToFile(file)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun restoreFromLocal(file: File): Result<Unit> = try {
        database.restoreFromFile(file)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getLastBackupDate(): Long? = try {
        val userId = auth.currentUser?.uid ?: return null

        storage.reference
            .child("backups")
            .child(userId)
            .listAll()
            .await()
            .items
            .maxByOrNull { it.name }
            ?.metadata
            ?.await()
            ?.creationTimeMillis
    } catch (e: Exception) {
        null
    }

    override suspend fun scheduleAutomaticBackup(intervalHours: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            intervalHours.toLong(),
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                backupWorkName,
                ExistingPeriodicWorkPolicy.REPLACE,
                backupRequest
            )
    }

    override suspend fun cancelAutomaticBackup() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(backupWorkName)
    }

    private fun createTempBackupFile(): File {
        return File.createTempFile("backup", ".db", context.cacheDir)
    }

    private fun File.toUri() = android.net.Uri.fromFile(this)
}

private fun RoomDatabase.backupToFile(file: File) {
    close()
    File(openHelper.writableDatabase.path!!).copyTo(file, overwrite = true)
    openHelper.writableDatabase // Reopen database
}

private fun RoomDatabase.restoreFromFile(file: File) {
    close()
    file.copyTo(File(openHelper.writableDatabase.path!!), overwrite = true)
    openHelper.writableDatabase // Reopen database
}

class BackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val backupService = ServiceLocator.getBackupService(applicationContext)

        return backupService.backupToFirebase().fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        )
    }
} 
