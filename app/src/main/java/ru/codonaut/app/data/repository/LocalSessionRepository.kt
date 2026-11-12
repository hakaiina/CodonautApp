package ru.codonaut.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.codonaut.app.data.local.preferences.ProfilePreferences
import ru.codonaut.app.domain.model.UserProfile
import ru.codonaut.app.domain.repository.ProfileRepository
import ru.codonaut.app.domain.repository.SessionRepository

// это конкретная реализация интерфейса SessionRepository через Room

class LocalSessionRepository(
    private val profileRepository: ProfileRepository,
    private val preferences: ProfilePreferences
) : SessionRepository {

    // защищает последовательность создания и сохранения гостевого профиля
    // от одновременного выполнения внутри одного экземпляра репозитория
    private val mutex = Mutex()

    override val activeProfileId: Flow<String?> =
        preferences.activeProfileId

    override suspend fun getActiveProfile(): UserProfile? {
        val profileId = preferences.getActiveProfileId()
            ?: return null

        return profileRepository.getById(profileId)
    }

    // логика сценария «Продолжить как гость»
    override suspend fun continueAsGuest(): UserProfile =
        mutex.withLock {

            val guestId = preferences.getGuestProfileId()

            val existingGuest = guestId?.let { id -> // выполняем действие когда значение != null
                profileRepository.getById(id)
            }?.takeIf { profile ->
                profile.authUid == null
            }

            val guest = existingGuest
                ?: profileRepository.createGuestProfile()

            preferences.activateGuest(guest.profileId)

            guest
        }

    override suspend fun activateProfile(
        profileId: String
    ): Boolean = mutex.withLock {

        val profile = profileRepository.getById(profileId)
            ?: return@withLock false

        preferences.activateProfile(profile.profileId)

        true
    }

    override suspend fun clearActiveProfile() {
        preferences.clearActiveProfile()
    }
}
