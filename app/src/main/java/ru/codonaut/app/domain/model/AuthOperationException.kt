package ru.codonaut.app.domain.model

// модель для ошибок авторизации

enum class AuthFailureReason {
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    NETWORK,
    TOO_MANY_REQUESTS,
    ACCOUNT_DISABLED,
    UNKNOWN
}

class AuthOperationException(
    val reason: AuthFailureReason,
    cause: Throwable
) : Exception("Authentication failed: $reason", cause)