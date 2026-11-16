package ru.codonaut.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.codonaut.app.data.local.dao.UserProfileDao
import ru.codonaut.app.data.local.entity.UserProfileEntity
import ru.codonaut.app.domain.model.UserProfile
import ru.codonaut.app.domain.repository.ProfileRepository
import java.util.UUID

// это конкретная реализация интерфейса ProfileRepository через Room

class LocalProfileRepository(
    private val dao: UserProfileDao // передаем зависимости через конструктор
) : ProfileRepository {

    override suspend fun createGuestProfile(
        nickname: String?
    ): UserProfile {
        val entity = UserProfileEntity(
            profileId = UUID.randomUUID().toString(), // генерим UUID при создании профиля
            authUid = null,
            nickname = nickname?.trim()
                ?.takeIf { it.isNotEmpty() },
            createdAt = System.currentTimeMillis()
        )

        dao.insert(entity)

        return entity.toDomain()
    }

    override suspend fun getById(
        profileId: String
    ): UserProfile? {
        return dao.getById(profileId)?.toDomain()
    }

    override suspend fun getByAuthUid(
        authUid: String
    ): UserProfile? {
        return dao.getByAuthUid(authUid)?.toDomain()
    }

    override fun observeById(
        profileId: String
    ): Flow<UserProfile?> {
        return dao.observeById(profileId).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun updateNickname(
        profileId: String,
        nickname: String?
    ): Boolean {
        val normalizedNickname = nickname?.trim()
            ?.takeIf { it.isNotEmpty() }

        return dao.updateNickname(
            profileId = profileId,
            nickname = normalizedNickname
        ) > 0
    }

    override suspend fun attachAuthUid(
        profileId: String,
        authUid: String
    ): Boolean {
        require(authUid.isNotBlank()) {
            "Firebase UID must not be blank"
        }

        val updatedRows = dao.attachAuthUid(
            profileId = profileId,
            authUid = authUid
        )

        if (updatedRows > 0) {
            return true
        }

        // повторная привязка того же UID считается успешной.
        return dao.getById(profileId)?.authUid == authUid
    }

    private fun UserProfileEntity.toDomain(): UserProfile {
        return UserProfile(
            profileId = profileId,
            authUid = authUid,
            nickname = nickname,
            createdAt = createdAt
        )
    }

    // сразу создаём профиль с authUid, не превращая его предварительно в гостевой
    override suspend fun createAccountProfile(
        authUid: String,
        nickname: String?
    ): UserProfile {
        require(authUid.isNotBlank()) {
            "Firebase UID must not be blank"
        }

        val entity = UserProfileEntity(
            profileId = UUID.randomUUID().toString(),
            authUid = authUid,
            nickname = nickname?.trim()
                ?.takeIf { it.isNotEmpty() },
            createdAt = System.currentTimeMillis()
        )

        dao.insert(entity)

        return entity.toDomain()
    }
}
