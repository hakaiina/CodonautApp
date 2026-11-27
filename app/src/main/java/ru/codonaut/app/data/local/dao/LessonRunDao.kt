package ru.codonaut.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.codonaut.app.data.local.entity.LessonRunEntity

@Dao
interface LessonRunDao {
    @Query("SELECT * FROM lesson_runs WHERE run_id = :runId LIMIT 1")
    suspend fun getById(runId: String): LessonRunEntity?

    @Query("SELECT * FROM lesson_runs WHERE profile_id = :profileId AND lesson_id = :lessonId AND completed_at IS NULL ORDER BY created_at DESC LIMIT 1")
    suspend fun getActive(profileId: String, lessonId: String): LessonRunEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(run: LessonRunEntity)

    @Update
    suspend fun update(run: LessonRunEntity)
}
