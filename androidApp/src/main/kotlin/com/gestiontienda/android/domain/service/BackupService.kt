package com.gestiontienda.android.domain.service

import java.io.File

interface BackupService {
    suspend fun backupToFirebase(): Result<Unit>
    suspend fun restoreFromFirebase(): Result<Unit>
    suspend fun createLocalBackup(file: File): Result<Unit>
    suspend fun restoreFromLocal(file: File): Result<Unit>
    suspend fun getLastBackupDate(): Long?
    suspend fun scheduleAutomaticBackup(intervalHours: Int)
    suspend fun cancelAutomaticBackup()
} 
