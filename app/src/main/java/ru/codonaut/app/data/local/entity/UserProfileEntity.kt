package ru.codonaut.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// UserProfileEntity - модель хранения данных пользователя (работает локально)
// здесь просто описываем данные, репозиторий же управляет их созданием

@Entity( // сообщаем Room, что класс соответствует таблице SQLite
    tableName = "user_profiles",
    indices = [
        Index(value = ["auth_uid"], unique = true)
    ]
)
data class UserProfileEntity(

    @PrimaryKey
    @ColumnInfo(name = "profile_id") // задаем имя столбца в БД
    val profileId: String,

    @ColumnInfo(name = "auth_uid")
    val authUid: String? = null, // "?" - может быть null

    @ColumnInfo(name = "nickname")
    val nickname: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)