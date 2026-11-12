package ru.codonaut.app.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.codonaut.app.domain.model.UserProfile

// контракт, который объединяет работу с локальным профилем и его активной сессией
// SessionRepository управляет тем, какой профиль используется в данный момент

interface SessionRepository {

    // поток Id активного профиля
    val activeProfileId: Flow<String?>

    // получение активного профиля
    suspend fun getActiveProfile(): UserProfile?

    // создание/восставновление гостя
    suspend fun continueAsGuest(): UserProfile

    // переключение на существующий профиль
    suspend fun activateProfile(profileId: String): Boolean

    // сброс активного профиля
    suspend fun clearActiveProfile()
}