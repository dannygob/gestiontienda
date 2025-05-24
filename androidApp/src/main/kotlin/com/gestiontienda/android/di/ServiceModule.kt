package com.gestiontienda.android.di

import com.gestiontienda.android.data.service.BackupServiceImpl
import com.gestiontienda.android.domain.service.BackupService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindBackupService(
        service: BackupServiceImpl,
    ): BackupService
} 
