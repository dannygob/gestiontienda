package com.gestiontienda.android.di

import android.content.Context
import com.gestiontienda.android.domain.service.BackupService
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackupWorkerEntryPoint {
    fun getBackupService(): BackupService
}

object ServiceLocator {
    fun getBackupService(context: Context): BackupService {
        val hiltEntryPoint = EntryPoints.get(
            context.applicationContext,
            BackupWorkerEntryPoint::class.java
        )
        return hiltEntryPoint.getBackupService()
    }
} 
