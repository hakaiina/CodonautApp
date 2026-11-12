package ru.codonaut.app.domain.model

// UserProfile модель предметной области
data class UserProfile(
    val profileId: String,
    val authUid: String?,
    val nickname: String?,
    val createdAt: Long
)