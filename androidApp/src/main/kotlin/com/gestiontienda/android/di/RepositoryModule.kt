package com.gestiontienda.android.di

import com.gestiontienda.android.data.repository.AuthRepositoryImpl
import com.gestiontienda.android.data.repository.ProductRepositoryImpl
import com.gestiontienda.android.data.repository.SaleRepositoryImpl
import com.gestiontienda.android.domain.repository.AuthRepository
import com.gestiontienda.android.domain.repository.ProductRepository
import com.gestiontienda.android.domain.repository.SaleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        repository: ProductRepositoryImpl,
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindSaleRepository(
        repository: SaleRepositoryImpl,
    ): SaleRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repository: AuthRepositoryImpl,
    ): AuthRepository
} 
