package ru.codonaut.app.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.codonaut.app.domain.model.PendingAccountLink
import androidx.datastore.preferences.core.booleanPreferencesKey

// DataStore способ хранения простых данных настроек пользователя в Android
// ProfilePreferences сохраняет идентификатор активного  пользователя

// использование одного экземпляра DataStore
private val Context.sessionDataStore by preferencesDataStore(
    name = "codonaut_session"
)

class ProfilePreferences(context: Context) {
    private val dataStore = context.applicationContext.sessionDataStore

    private object Keys {
        val ACTIVE_PROFILE_ID =
            stringPreferencesKey("active_profile_id") // создаем типизированный ключ для сохранения строки

        val GUEST_PROFILE_ID =
            stringPreferencesKey("guest_profile_id")

        val PENDING_PROFILE_ID =
            stringPreferencesKey("pending_link_profile_id")

        val PENDING_AUTH_UID =
            stringPreferencesKey("pending_link_auth_uid")

        val PENDING_NICKNAME =
            stringPreferencesKey("pending_link_nickname")

        // онбординг пройден
        val ONBOARDING_COMPLETED =
            booleanPreferencesKey("onboarding_completed")
    }

    val activeProfileId: Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[Keys.ACTIVE_PROFILE_ID]
        }

    suspend fun getActiveProfileId(): String? {
        return activeProfileId.first()
    }

    suspend fun getGuestProfileId(): String? {
        return dataStore.data.first()[Keys.GUEST_PROFILE_ID]
    }

    suspend fun activateGuest(profileId: String) {
        dataStore.edit { preferences ->
            preferences[Keys.GUEST_PROFILE_ID] = profileId
            preferences[Keys.ACTIVE_PROFILE_ID] = profileId
        }
    }

    suspend fun activateProfile(profileId: String) {
        dataStore.edit { preferences ->
            preferences[Keys.ACTIVE_PROFILE_ID] = profileId
        }
    }

    suspend fun clearActiveProfile() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.ACTIVE_PROFILE_ID)
        }
    }

    suspend fun savePendingAccountLink(
        link: PendingAccountLink
    ) {
        dataStore.edit { preferences ->
            preferences[Keys.PENDING_PROFILE_ID] = link.profileId
            preferences[Keys.PENDING_AUTH_UID] = link.authUid
            preferences[Keys.PENDING_NICKNAME] = link.nickname
        }
    }

    suspend fun getPendingAccountLink(): PendingAccountLink? {
        val preferences = dataStore.data.first()

        val profileId = preferences[Keys.PENDING_PROFILE_ID]
            ?: return null

        val authUid = preferences[Keys.PENDING_AUTH_UID]
            ?: return null

        val nickname = preferences[Keys.PENDING_NICKNAME]
            ?: return null

        return PendingAccountLink(
            profileId = profileId,
            authUid = authUid,
            nickname = nickname
        )
    }

    suspend fun clearPendingAccountLink() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.PENDING_PROFILE_ID)
            preferences.remove(Keys.PENDING_AUTH_UID)
            preferences.remove(Keys.PENDING_NICKNAME)
        }
    }

    // данные функции запоминают, что пользователь прошел онбординг не показывают его вновь
    suspend fun isOnboardingCompleted(): Boolean {
        return dataStore.data.first()[Keys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun completeOnboarding() {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = true
        }
    }
}