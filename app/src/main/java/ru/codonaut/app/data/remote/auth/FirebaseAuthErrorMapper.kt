package ru.codonaut.app.data.remote.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import ru.codonaut.app.domain.model.AuthFailureReason
import ru.codonaut.app.domain.model.AuthOperationException

// обработка ошибок серверной авторизации

internal fun Exception.toAuthOperationException(): AuthOperationException {

    val reason = when (this) {

        is FirebaseNetworkException ->
            AuthFailureReason.NETWORK

        is FirebaseTooManyRequestsException ->
            AuthFailureReason.TOO_MANY_REQUESTS

        is FirebaseAuthUserCollisionException ->
            AuthFailureReason.EMAIL_ALREADY_IN_USE

        // Проверяем раньше InvalidCredentialsException,
        // поскольку WeakPasswordException наследуется от неё.
        is FirebaseAuthWeakPasswordException ->
            AuthFailureReason.WEAK_PASSWORD

        is FirebaseAuthInvalidUserException ->
            if (errorCode == "ERROR_USER_DISABLED") {
                AuthFailureReason.ACCOUNT_DISABLED
            } else {
                AuthFailureReason.INVALID_CREDENTIALS
            }

        is FirebaseAuthInvalidCredentialsException ->
            AuthFailureReason.INVALID_CREDENTIALS

        is FirebaseAuthException ->
            when (errorCode) {
                "ERROR_TOO_MANY_REQUESTS" ->
                    AuthFailureReason.TOO_MANY_REQUESTS

                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    AuthFailureReason.EMAIL_ALREADY_IN_USE

                "ERROR_WEAK_PASSWORD" ->
                    AuthFailureReason.WEAK_PASSWORD

                "ERROR_USER_DISABLED" ->
                    AuthFailureReason.ACCOUNT_DISABLED

                "ERROR_INVALID_LOGIN_CREDENTIALS",
                "ERROR_WRONG_PASSWORD",
                "ERROR_USER_NOT_FOUND",
                "ERROR_INVALID_EMAIL" ->
                    AuthFailureReason.INVALID_CREDENTIALS

                else ->
                    AuthFailureReason.UNKNOWN
            }

        else ->
            AuthFailureReason.UNKNOWN
    }

    return AuthOperationException(
        reason = reason,
        cause = this
    )
}
