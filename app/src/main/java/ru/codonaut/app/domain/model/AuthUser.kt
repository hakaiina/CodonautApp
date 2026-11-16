package ru.codonaut.app.domain.model

data class AuthUser (
    val uid: String,
    val email: String?,
    val isEmailVerified: Boolean
)