package ru.codonaut.app.domain.usecase

import ru.codonaut.app.domain.model.PendingAccountLink
import ru.codonaut.app.domain.model.UserProfile
import ru.codonaut.app.domain.repository.AuthRepository
import ru.codonaut.app.domain.repository.ProfileRepository
import ru.codonaut.app.domain.repository.SessionRepository

// данный класс управляет единым пользовательским сценарием, который затрагивает несколько источников данных
// связывает работу: authRepository, profileRepository и sessionRepository
/* сначала создаётся Firebase-аккаунт
    затем берём текущий локальный профиль и проверяем, что он гостевой
    если да, записываем полученный UID в существующий профиль и обновляем никнейм
*/

class AccountSessionCoordinator(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SessionRepository
) {

    suspend fun register(
        email: String,
        password: String,
        nickname: String
    ): UserProfile {

        // находим гостя ДО отправки запроса Firebase.
        val guest = sessionRepository.getActiveProfile()
            ?.takeIf { it.authUid == null }

        // создаём серверную учётную запись
        val authUser = authRepository.register(email, password)

        // проверяем, работал ли пользователь как гость
        if (guest == null) {
            return activateAccountProfile(
                uid = authUser.uid,
                nickname = nickname
            )
        }

        // запоминаем конкретный UID и локальный профиль.
        sessionRepository.savePendingAccountLink(
            PendingAccountLink(
                profileId = guest.profileId,
                authUid = authUser.uid,
                nickname = nickname
            )
        )

        return recoverPendingLink(authUser.uid)
            ?: error("Не удалось восстановить привязку аккаунта")
    }

    // поиск локального профиля по uid, если он отсутсвует, то создаем новый
    suspend fun signIn(
        email: String,
        password: String
    ): UserProfile {
        val authUser = authRepository.signIn(email, password)

        // восстанавливаем привязку только для того же UID.
        return recoverPendingLink(authUser.uid)
            ?: activateAccountProfile(authUser.uid)
    }

    // восстановление сессии
    // сначала проверяем Firebase, если юзер авторизован, то восставнавливаем его лок. профиль
    suspend fun restoreSession(): UserProfile? {
        val authUser = authRepository.getCurrentUser()

        if (authUser != null) {
            return recoverPendingLink(authUser.uid)
                ?: activateAccountProfile(authUser.uid)
        }

        val activeProfile = sessionRepository.getActiveProfile()

        // нельзя считать локально сохранённый ID доказательством серверной авторизации
        if (activeProfile?.authUid != null) {
            sessionRepository.clearActiveProfile()
            return null
        }

        return activeProfile
    }

    suspend fun continueAsGuest(): UserProfile {
        authRepository.signOut()
        return sessionRepository.continueAsGuest()
    }

    suspend fun signOut() {
        authRepository.signOut()
        sessionRepository.clearActiveProfile()
    }

    private suspend fun recoverPendingLink(
        uid: String
    ): UserProfile? {
        val pending = sessionRepository.getPendingAccountLink()
            ?: return null

        // чужой UID не может получить гостевой прогресс.
        if (pending.authUid != uid) {
            return null
        }

        val profile = profileRepository.getById(pending.profileId)
            ?: error("Локальный профиль для привязки не найден")

        check(profile.authUid == null || profile.authUid == uid) {
            "Профиль уже привязан к другому аккаунту"
        }

        check(
            profileRepository.attachAuthUid(
                profileId = pending.profileId,
                authUid = uid
            )
        ) {
            "Не удалось сохранить Firebase UID"
        }

        check(
            profileRepository.updateNickname(
                profileId = pending.profileId,
                nickname = pending.nickname
            )
        ) {
            "Не удалось сохранить никнейм"
        }

        check(
            sessionRepository.activateProfile(pending.profileId)
        ) {
            "Не удалось активировать профиль"
        }

        val linkedProfile = requireNotNull(
            profileRepository.getById(pending.profileId)
        )

        // Удаляем запись только после успешной привязки.
        sessionRepository.clearPendingAccountLink()

        return linkedProfile
    }

    private suspend fun activateAccountProfile(
        uid: String,
        nickname: String? = null
    ): UserProfile {
        val profile = profileRepository.getByAuthUid(uid)
            ?: profileRepository.createAccountProfile(
                authUid = uid,
                nickname = nickname
            )

        check(
            sessionRepository.activateProfile(profile.profileId)
        ) {
            "Не удалось активировать локальный профиль"
        }

        return profile
    }
}