package ru.codonaut.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.codonaut.app.data.local.dao.LessonProgressDao
import ru.codonaut.app.data.local.dao.SelectedLanguageDao
import ru.codonaut.app.data.local.dao.TaskAttemptDao
import ru.codonaut.app.data.local.dao.UserProfileDao
import ru.codonaut.app.data.local.dao.LessonRunDao
import ru.codonaut.app.data.local.entity.LessonProgressEntity
import ru.codonaut.app.data.local.entity.SelectedLanguageEntity
import ru.codonaut.app.data.local.entity.TaskAttemptEntity
import ru.codonaut.app.data.local.entity.UserProfileEntity
import ru.codonaut.app.data.local.entity.LessonRunEntity

// здесь создаем саму локальную БД
// реализацию данного класса генерирует Room через KSP

@Database(
    // получаем модели
    entities = [
        UserProfileEntity::class,
        SelectedLanguageEntity::class,
        LessonProgressEntity::class,
        TaskAttemptEntity::class,
        LessonRunEntity::class
    ],
    version = 2,
    exportSchema = true
)

// абстрактный класс - потому что реализацию БД автоматически сгенерирует Room через KSP
abstract class CodonautDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao

    abstract fun selectedLanguageDao(): SelectedLanguageDao

    abstract fun lessonProgressDao(): LessonProgressDao

    abstract fun taskAttemptDao(): TaskAttemptDao

    abstract fun lessonRunDao(): LessonRunDao
}
