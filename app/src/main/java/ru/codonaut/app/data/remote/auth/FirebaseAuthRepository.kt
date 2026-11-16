package ru.codonaut.app.data.remote.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import ru.codonaut.app.domain.model.AuthUser
import ru.codonaut.app.domain.repository.AuthRepository


class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth // передаем FirebaseAuth через конструктор
) : AuthRepository {

    override fun getCurrentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toDomain()
    }

    override suspend fun register(
        email: String,
        password: String
    ): AuthUser {
        val result = firebaseAuth
            .createUserWithEmailAndPassword(
                email.trim(),
                password
            )
            .await() // приостанавливает корутину до получения результата, не блокируя основной поток

        val user = result.user
            ?: error("Firebase не вернул созданного пользователя")

        return user.toDomain()
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): AuthUser {
        val result = firebaseAuth
            .signInWithEmailAndPassword(
                email.trim(),
                password
            )
            .await()

        val user = result.user
            ?: error("Firebase не вернул пользователя после входа")

        return user.toDomain()
    }

    override suspend fun sendPasswordReset(
        email: String
    ) {
        firebaseAuth
            .sendPasswordResetEmail(email.trim())
            .await()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    // преобразование FirebaseUser в AuthUser
    private fun FirebaseUser.toDomain(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            isEmailVerified = isEmailVerified
        )
    }
}
