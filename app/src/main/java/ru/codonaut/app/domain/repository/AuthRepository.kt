package ru.codonaut.app.domain.repository

import ru.codonaut.app.domain.model.AuthUser

// контракт операций авторизации

interface AuthRepository {

    fun getCurrentUser(): AuthUser?

    suspend fun register(
        email: String,
        password: String
    ): AuthUser

    suspend fun signIn(
        email: String,
        password: String
    ): AuthUser

    suspend fun sendPasswordReset(email: String)

    fun signOut()
}