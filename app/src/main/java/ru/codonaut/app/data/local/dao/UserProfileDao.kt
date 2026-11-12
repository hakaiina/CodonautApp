package ru.codonaut.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.codonaut.app.data.local.entity.UserProfileEntity

// DAO (Data Access Object) описывает операции с таблицей
@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    // suspend означает, что операция может выполняться асинхронно без блокировки основного потока
    suspend fun insert(profile: UserProfileEntity)

    @Query(
        "SELECT * FROM user_profiles " +
                "WHERE profile_id = :profileId LIMIT 1"
    )
    suspend fun getById(profileId: String): UserProfileEntity?

    @Query(
        "SELECT * FROM user_profiles " +
                "WHERE auth_uid = :authUid LIMIT 1"
    )
    suspend fun getByAuthUid(authUid: String): UserProfileEntity?

    @Query(
        "SELECT * FROM user_profiles " +
                "WHERE profile_id = :profileId LIMIT 1"
    )
    // получаем обновления при изменении записи в Room
    fun observeById(profileId: String): Flow<UserProfileEntity?> // поток значений

    @Query(
        "UPDATE user_profiles SET auth_uid = :authUid " +
                "WHERE profile_id = :profileId AND auth_uid IS NULL"
    )
    // функция возвраащет количество измененных строк, чтобы понять удалось ли связать гостевой профиль с аккаунтом
    suspend fun attachAuthUid(
        profileId: String,
        authUid: String
    ): Int

    @Query(
        """
    UPDATE user_profiles
    SET nickname = :nickname
    WHERE profile_id = :profileId
    """
    )
    suspend fun updateNickname(
        profileId: String,
        nickname: String?
    ): Int
}