package ru.codonaut.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

// LessonProgressEntity - модель описания прогресса пользователя (локально)

@Entity(
    tableName = "lesson_progress",
    primaryKeys = ["profile_id", "lesson_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["profile_id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class LessonProgressEntity(
    @ColumnInfo(name = "profile_id")
    val profileId: String,

    @ColumnInfo(name = "lesson_id")
    val lessonId: String,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "xp_earned")
    val xpEarned: Int = 0
)