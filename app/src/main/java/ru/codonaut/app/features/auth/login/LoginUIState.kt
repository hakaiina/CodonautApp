package ru.codonaut.app.features.auth.login

// это состояние экрана авторизации

data class LoginUiState(
    // текущие значения полей
    val email: String = "",
    val password: String = "",

    // ошибки конкретных полей
    val emailError: String? = null,
    val passwordError: String? = null,
    // ошибка операции входа или гостевого режима
    val generalError: String? = null,
    val passwordResetMessage: String? = null,

    // выполняется асинхронная операция
    val isLoading: Boolean = false,
    // юзер успешно вошёл или выбрал гостевой режим
    val isCompleted: Boolean = false,
)