package ru.codonaut.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// TaskAttemptEntity - модель описания прогресса задания (локально)

@Entity(
    tableName = "task_attempts",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["profile_id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["profile_id", "task_id", "answered_at"]
        ),
        Index(value = ["run_id", "task_id"])
    ]
)

data class TaskAttemptEntity(
    @PrimaryKey
    @ColumnInfo(name = "attempt_id")
    val attemptId: String,

    @ColumnInfo(name = "profile_id")
    val profileId: String,

    @ColumnInfo(name = "task_id")
    val taskId: String,

    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean,

    @ColumnInfo(name = "answered_at")
    val answeredAt: Long,

    @ColumnInfo(name = "run_id")
    val runId: String? = null,

    @ColumnInfo(name = "is_reinforcement", defaultValue = "0")
    val isReinforcement: Boolean = false
)
