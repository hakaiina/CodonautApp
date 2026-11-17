package ru.codonaut.app.features.auth.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.codonaut.app.CodonautApplication
import ru.codonaut.app.domain.usecase.AccountSessionCoordinator
import ru.codonaut.app.features.auth.validation.AuthInputValidator
import ru.codonaut.app.domain.model.AuthFailureReason
import ru.codonaut.app.domain.model.AuthOperationException
import ru.codonaut.app.features.auth.error.toAuthErrorMessage

// RegistrationViewModel управляет состоянием экрана регистрации и координирует процесс регистрации

class RegistrationViewModel(
    private val accountSessionCoordinator: AccountSessionCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegistrationUiState()
    )

    val uiState = _uiState.asStateFlow()

    fun onNicknameChanged(value: String) {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(
            nickname = value,
            nicknameError = null,
            generalError = null
        )
    }

    fun onEmailChanged(value: String) {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(
            email = value,
            emailError = null,
            generalError = null
        )
    }

    fun onPasswordChanged(value: String) {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = null,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun onConfirmPasswordChanged(value: String) {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(
            confirmPassword = value,
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun register() {
        val current = _uiState.value

        if (current.isLoading || current.isCompleted) return

        val nicknameError =
            AuthInputValidator.validateNickname(current.nickname)

        val emailError =
            AuthInputValidator.validateEmail(current.email)

        val passwordError =
            AuthInputValidator.validateRegistrationPassword(
                current.password
            )

        val confirmationError =
            AuthInputValidator.validatePasswordConfirmation(
                current.password,
                current.confirmPassword
            )

        // проверяем сразу несколько полей и создаем список результатов валидации
        val hasErrors = listOf(
            nicknameError,
            emailError,
            passwordError,
            confirmationError
        ).any { it != null } // возвращаем true, если хотя бы один элемент удовлетворяет условию

        if (hasErrors) {
            _uiState.value = current.copy(
                nicknameError = nicknameError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmationError,
                generalError = null
            )
            return
        }

        _uiState.value = current.copy(
            isLoading = true,
            generalError = null
        )

        viewModelScope.launch {
            try {
                accountSessionCoordinator.register(
                    nickname = current.nickname.trim(),
                    email = current.email.trim(),
                    password = current.password
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCompleted = true,
                    password = "",
                    confirmPassword = ""
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                val reason = (e as? AuthOperationException)?.reason
                val message = e.toAuthErrorMessage()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,

                    emailError =
                        if (reason == AuthFailureReason.EMAIL_ALREADY_IN_USE) {
                            message
                        } else {
                            null
                        },

                    passwordError =
                        if (reason == AuthFailureReason.WEAK_PASSWORD) {
                            message
                        } else {
                            null
                        },

                    generalError =
                        if (
                            reason != AuthFailureReason.EMAIL_ALREADY_IN_USE &&
                            reason != AuthFailureReason.WEAK_PASSWORD
                        ) {
                            if (e is AuthOperationException) {
                                message
                            } else {
                                "Не удалось завершить регистрацию. " +
                                        "Если аккаунт уже был создан, попробуйте войти."
                            }
                        } else {
                            null
                        }
                )
            }
        }
    }

    fun onCompletionHandled() {
        _uiState.value = _uiState.value.copy(
            isCompleted = false
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CodonautApplication

                RegistrationViewModel(
                    accountSessionCoordinator =
                        application.container.accountSessionCoordinator
                )
            }
        }
    }
}
