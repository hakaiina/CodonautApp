package ru.codonaut.app.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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
}