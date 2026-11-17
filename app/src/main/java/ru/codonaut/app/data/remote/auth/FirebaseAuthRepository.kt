package ru.codonaut.app.data.remote.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import ru.codonaut.app.domain.model.AuthUser
import ru.codonaut.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.CancellationException


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
        try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(
                    email.trim(),
                    password
                )
                .await()

            val user = result.user
                ?: error("Firebase не вернул созданного пользователя")

            return user.toDomain()

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            throw e.toAuthOperationException()
        }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): AuthUser {
        try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(
                    email.trim(),
                    password
                )
                .await()

            val user = result.user
                ?: error("Firebase не вернул пользователя после входа")

            return user.toDomain()

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            throw e.toAuthOperationException()
        }
    }

    override suspend fun sendPasswordReset(email: String) {
        try {
            firebaseAuth
                .sendPasswordResetEmail(email.trim())
                .await()

        } catch (e: CancellationException) {
            throw e

        } catch (e: FirebaseAuthInvalidUserException) {
            if (e.errorCode != "ERROR_USER_NOT_FOUND") {
                throw e.toAuthOperationException()
            }

            // не раскрываем существование email

        } catch (e: Exception) {
            throw e.toAuthOperationException()
        }
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
