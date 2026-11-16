package ru.codonaut.app.features.auth.registration

// это состояние экрана регистрации

data class RegistrationUiState(
    // данные формы
    val nickname: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    // ошибки
    val nicknameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,

    // состояния операций
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false
)