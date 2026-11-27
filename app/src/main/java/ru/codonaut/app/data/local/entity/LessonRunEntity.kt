package ru.codonaut.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lesson_runs",
    foreignKeys = [ForeignKey(
        entity = UserProfileEntity::class,
        parentColumns = ["profile_id"],
        childColumns = ["profile_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["profile_id", "lesson_id", "completed_at"])]
)
data class LessonRunEntity(
    @PrimaryKey @ColumnInfo(name = "run_id") val runId: String,
    @ColumnInfo(name = "profile_id") val profileId: String,
    @ColumnInfo(name = "lesson_id") val lessonId: String,
    @ColumnInfo(name = "phase") val phase: String = "THEORY",
    @ColumnInfo(name = "theory_index") val theoryIndex: Int = 0,
    @ColumnInfo(name = "task_order") val taskOrder: Int = 0,
    @ColumnInfo(name = "wrong_attempts") val wrongAttempts: Int = 0,
    @ColumnInfo(name = "reinforcement_offered") val reinforcementOffered: Boolean = false,
    @ColumnInfo(name = "reinforcement_pending") val reinforcementPending: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null
)
