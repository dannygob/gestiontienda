package com.gestiontienda.android.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String, name: String): Result<Unit>
    suspend fun signOut()
    fun isUserAuthenticated(): Flow<Boolean>
    fun getCurrentUserEmail(): String?
    fun getCurrentUserName(): String?
    suspend fun updateUserProfile(name: String): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
} 
