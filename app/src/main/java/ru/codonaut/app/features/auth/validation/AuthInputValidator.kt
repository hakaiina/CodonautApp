package ru.codonaut.app.features.auth.validation

import android.util.Patterns

// валидатор для авторизации и регистрации

object AuthInputValidator {

    fun validateEmail(email: String): String? { // если null, то поле прошло проверку
        val normalizedEmail = email.trim()

        return when {
            normalizedEmail.isEmpty() ->
                "Введите адрес электронной почты"

            !Patterns.EMAIL_ADDRESS
                .matcher(normalizedEmail)
                .matches() ->
                "Некорректный адрес электронной почты"

            else -> null
        }
    }

    fun validateLoginPassword(password: String): String? { // если null, то поле прошло проверку
        return if (password.isEmpty()) {
            "Введите пароль"
        } else {
            null
        }
    }

    fun validateNickname(nickname: String): String? {
        val normalized = nickname.trim()

        return when {
            normalized.isEmpty() ->
                "Введите никнейм"

            normalized.length < 2 ->
                "Никнейм должен содержать минимум 2 символа"

            normalized.length > 24 ->
                "Никнейм не должен превышать 24 символа"

            else -> null
        }
    }

    fun validateRegistrationPassword(password: String): String? {
        return when {
            password.isEmpty() ->
                "Введите пароль"

            password.length < 8 ->
                "Пароль должен содержать минимум 8 символов"

            else -> null
        }
    }

    fun validatePasswordConfirmation(
        password: String,
        confirmation: String
    ): String? {
        return when {
            confirmation.isEmpty() ->
                "Подтвердите пароль"

            password != confirmation ->
                "Пароли не совпадают"

            else -> null
        }
    }
}