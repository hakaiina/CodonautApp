package ru.codonaut.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

// SelectedLanguageEntity - модель описания данных выбранных языков у пользователя (локально)

@Entity(
    tableName = "selected_languages",
    primaryKeys = ["profile_id", "language_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["profile_id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class SelectedLanguageEntity(
    @ColumnInfo(name = "profile_id")
    val profileId: String,

    @ColumnInfo(name = "language_id")
    val languageId: String
)