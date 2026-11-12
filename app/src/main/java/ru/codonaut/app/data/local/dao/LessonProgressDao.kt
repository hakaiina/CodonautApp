package ru.codonaut.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.codonaut.app.data.local.entity.LessonProgressEntity

@Dao
interface LessonProgressDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(
        progress: LessonProgressEntity
    ): Long

    @Query(
        """
        SELECT * FROM lesson_progress
        WHERE profile_id = :profileId
        ORDER BY lesson_id
        """
    )
    fun observeForProfile(
        profileId: String
    ): Flow<List<LessonProgressEntity>>

    @Query(
        """
        SELECT * FROM lesson_progress
        WHERE profile_id = :profileId
        AND lesson_id = :lessonId
        LIMIT 1
        """
    )
    suspend fun getByLessonId(
        profileId: String,
        lessonId: String
    ): LessonProgressEntity?

    @Query(
        """
        UPDATE lesson_progress
        SET completed_at = :completedAt,
            xp_earned = :xpEarned
        WHERE profile_id = :profileId
        AND lesson_id = :lessonId
        AND completed_at IS NULL
        """
    )
    suspend fun completeIfNotCompleted(
        profileId: String,
        lessonId: String,
        completedAt: Long,
        xpEarned: Int
    ): Int

    @Query(
        """
        SELECT COALESCE(SUM(xp_earned), 0)
        FROM lesson_progress
        WHERE profile_id = :profileId
        """
    )
    fun observeTotalXp(
        profileId: String
    ): Flow<Long>
}