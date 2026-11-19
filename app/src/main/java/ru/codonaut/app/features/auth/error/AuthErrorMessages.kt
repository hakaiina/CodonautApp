package ru.codonaut.app.features.auth.error

import ru.codonaut.app.domain.model.AuthFailureReason
import ru.codonaut.app.domain.model.AuthOperationException

// храним сообщения об ошибках

fun Throwable.toAuthErrorMessage(): String {
    val reason = (this as? AuthOperationException)?.reason

    return when (reason) {
        AuthFailureReason.INVALID_CREDENTIALS ->
            "Неверный email или пароль."

        AuthFailureReason.EMAIL_ALREADY_IN_USE ->
            "Учетная запись с таким email уже существует. Авторизуйтесь, чтобы продолжить."

        AuthFailureReason.WEAK_PASSWORD ->
            "Пароль не соответствует требованиям безопасности."

        AuthFailureReason.NETWORK ->
            "Нет подключения к интернету. Проверьте сеть и попробуйте снова."

        AuthFailureReason.TOO_MANY_REQUESTS ->
            "Слишком много попыток. Попробуйте позже."

        AuthFailureReason.ACCOUNT_DISABLED ->
            "Учётная запись отключена."

        AuthFailureReason.UNKNOWN, null ->
            "Не удалось выполнить операцию. Попробуйте ещё раз."
    }
}