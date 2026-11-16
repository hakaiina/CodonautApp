package ru.codonaut.app.di

import android.content.Context
import androidx.room.Room
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
        ).build()
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
}