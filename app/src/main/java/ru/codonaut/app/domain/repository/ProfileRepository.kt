package ru.codonaut.app.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.codonaut.app.domain.model.UserProfile


// это интерфейс для управления данными профиля
interface ProfileRepository {

    // создание локального гостя
    suspend fun createGuestProfile(
        nickname: String? = null
    ): UserProfile

    // поиск пользователя по локальному Id
    suspend fun getById(
        profileId: String
    ): UserProfile?

    // поиск авторизованный профиль по Firebase UID
    suspend fun getByAuthUid(
        authUid: String
    ): UserProfile?

    // наблюдение за изменениями профиля
    fun observeById(
        profileId: String
    ): Flow<UserProfile?>

    // обновление никнейма
    suspend fun updateNickname(
        profileId: String,
        nickname: String?
    ): Boolean

    // связка профиля с учетной записью
    suspend fun attachAuthUid(
        profileId: String,
        authUid: String
    ): Boolean

    // создание профиля, если того нет
    suspend fun createAccountProfile(
        authUid: String,
        nickname: String? = null
    ): UserProfile
}