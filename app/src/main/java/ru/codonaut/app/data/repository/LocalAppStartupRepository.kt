package ru.codonaut.app.data.repository

import ru.codonaut.app.data.local.preferences.ProfilePreferences
import ru.codonaut.app.domain.repository.AppStartupRepository

// здесь реализуем AppStartupRepository

class LocalAppStartupRepository(
    private val preferences: ProfilePreferences
) : AppStartupRepository {

    override suspend fun isOnboardingCompleted(): Boolean {
        return preferences.isOnboardingCompleted()
    }

    override suspend fun completeOnboarding() {
        preferences.completeOnboarding()
    }
}