package ru.codonaut.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.codonaut.app.data.local.entity.TaskAttemptEntity

@Dao
interface TaskAttemptDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(
        attempt: TaskAttemptEntity
    )

    @Query(
        """
        SELECT * FROM task_attempts
        WHERE profile_id = :profileId
        AND task_id = :taskId
        ORDER BY answered_at DESC
        """
    )
    fun observeForTask(
        profileId: String,
        taskId: String
    ): Flow<List<TaskAttemptEntity>>

    @Query(
        """
        SELECT COUNT(*)
        FROM task_attempts
        WHERE profile_id = :profileId
        AND task_id = :taskId
        AND is_correct = 0
        """
    )
    suspend fun countIncorrectAttempts(
        profileId: String,
        taskId: String
    ): Int

    @Query(
        """
        SELECT * FROM task_attempts
        WHERE profile_id = :profileId
        ORDER BY answered_at DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentAttempts(
        profileId: String,
        limit: Int
    ): List<TaskAttemptEntity>
}