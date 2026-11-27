package ru.codonaut.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.codonaut.app.data.local.CodonautDatabase
import ru.codonaut.app.data.local.preferences.ProfilePreferences
import ru.codonaut.app.data.repository.LocalProfileRepository
import ru.codonaut.app.data.repository.LocalSessionRepository
import ru.codonaut.app.domain.repository.ProfileRepository
import ru.codonaut.app.domain.repository.SessionRepository
import com.google.firebase.auth.FirebaseAuth
import ru.codonaut.app.data.remote.auth.FirebaseAuthRepository
import ru.codonaut.app.domain.repository.AuthRepository
import ru.codonaut.app.domain.usecase.AccountSessionCoordinator
import ru.codonaut.app.data.repository.LocalAppStartupRepository
import ru.codonaut.app.domain.repository.AppStartupRepository
import ru.codonaut.app.data.repository.LocalSelectedLanguageRepository
import ru.codonaut.app.domain.repository.SelectedLanguageRepository
import ru.codonaut.app.data.repository.AssetCourseContentRepository
import ru.codonaut.app.data.repository.LocalLearningRepository

// di (Dependency Injection) - внедрение зависимостей

// здесь создаем экземпляр БД и предоставляем общие зависимости
// при первом обращении создаются база и репозиторий
// при последующих обращениях возвращаются уже существующие экземпляры

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    // by lazy - ленивая инициализация, объект создаётся только при первом обращении к свойству
    val database: CodonautDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            CodonautDatabase::class.java,
            "codonaut.db"
        ).addMigrations(MIGRATION_1_2).build()
    }

    val profileRepository: ProfileRepository by lazy {
        LocalProfileRepository(
            dao = database.userProfileDao()
        )
    }
    val profilePreferences: ProfilePreferences by lazy {
        ProfilePreferences(appContext)
    }

    val sessionRepository: SessionRepository by lazy {
        LocalSessionRepository(
            profileRepository = profileRepository,
            preferences = profilePreferences
        )
    }

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(
            firebaseAuth = firebaseAuth
        )
    }

    val accountSessionCoordinator: AccountSessionCoordinator by lazy {
        AccountSessionCoordinator(
            authRepository = authRepository,
            profileRepository = profileRepository,
            sessionRepository = sessionRepository
        )
    }

    val appStartupRepository: AppStartupRepository by lazy {
        LocalAppStartupRepository(profilePreferences)
    }

    val selectedLanguageRepository: SelectedLanguageRepository by lazy {
        LocalSelectedLanguageRepository(
            database = database,
            dao = database.selectedLanguageDao()
        )
    }

    val courseContentRepository by lazy { AssetCourseContentRepository(appContext) }
    val learningRepository by lazy { LocalLearningRepository(database) }

    private companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE task_attempts ADD COLUMN run_id TEXT")
                db.execSQL("ALTER TABLE task_attempts ADD COLUMN is_reinforcement INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_task_attempts_run_id_task_id ON task_attempts(run_id, task_id)")
                db.execSQL("CREATE TABLE IF NOT EXISTS lesson_runs (run_id TEXT NOT NULL, profile_id TEXT NOT NULL, lesson_id TEXT NOT NULL, phase TEXT NOT NULL, theory_index INTEGER NOT NULL, task_order INTEGER NOT NULL, wrong_attempts INTEGER NOT NULL, reinforcement_offered INTEGER NOT NULL, reinforcement_pending INTEGER NOT NULL, created_at INTEGER NOT NULL, completed_at INTEGER, PRIMARY KEY(run_id), FOREIGN KEY(profile_id) REFERENCES user_profiles(profile_id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lesson_runs_profile_id_lesson_id_completed_at ON lesson_runs(profile_id, lesson_id, completed_at)")
            }
        }
    }
}
