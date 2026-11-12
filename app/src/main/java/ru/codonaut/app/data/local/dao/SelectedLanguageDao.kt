package ru.codonaut.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.codonaut.app.data.local.entity.SelectedLanguageEntity

@Dao
interface SelectedLanguageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(
        selection: SelectedLanguageEntity
    ): Long

    @Query(
        """
        SELECT language_id
        FROM selected_languages
        WHERE profile_id = :profileId
        ORDER BY language_id
        """
    )
    fun observeSelectedLanguageIds(
        profileId: String
    ): Flow<List<String>>

    @Query(
        """
        DELETE FROM selected_languages
        WHERE profile_id = :profileId
        AND language_id = :languageId
        """
    )
    suspend fun delete(
        profileId: String,
        languageId: String
    ): Int
}