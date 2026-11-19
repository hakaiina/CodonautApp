package ru.codonaut.app.domain.repository

// контракт для настройки того, чтобы онбординг не перезапускался при каждом входе в приложение

interface AppStartupRepository {
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun completeOnboarding()
}