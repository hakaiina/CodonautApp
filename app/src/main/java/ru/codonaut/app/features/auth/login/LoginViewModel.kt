package ru.codonaut.app.features.auth.login

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

// LoginViewModel управляет состоянием экрана логина и координирует процесс аутентификации

class LoginViewModel(
    private val accountSessionCoordinator: AccountSessionCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState = _uiState.asStateFlow()

    // обработчики входа
    // игнорируют ввод, пока идёт загрузка
    // обновляют поле и сбрасывают ошибки, чтобы ошибка исчезала при исправлении
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
            generalError = null
        )
    }

    // авторизация по логину и паролю
    fun signIn() {
        val current = _uiState.value

        if (current.isLoading || current.isCompleted) return

        val emailError = AuthInputValidator
            .validateEmail(current.email)

        val passwordError = AuthInputValidator
            .validateLoginPassword(current.password)

        // если есть ошибки, то сохраняет их в состояние и выходит (запрос не отправляется)
        if (emailError != null || passwordError != null) {
            _uiState.value = current.copy(
                emailError = emailError,
                passwordError = passwordError,
                generalError = null
            )
            return
        }

        // иначе ставит isLoading = true и запускает корутину в viewModelScope
        _uiState.value = current.copy(
            isLoading = true,
            generalError = null
        )

        viewModelScope.launch {
            try {
                accountSessionCoordinator.signIn(
                    email = current.email.trim(),
                    password = current.password
                )
                // при успехе: isLoading = false, isCompleted = true, очищает пароль
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCompleted = true,
                    password = ""
                )
            // пробрасываем исключение, не прирывая работу
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = "Не удалось войти. Проверьте данные и подключение к интернету."
                )
            }
        }
    }

    // гостевой профиль
    fun continueAsGuest() {
        val current = _uiState.value

        // защита от повторного запуска операции (например, при двойном нажатии на кнопку)
        if (current.isLoading || current.isCompleted) return

        _uiState.value = current.copy(
            isLoading = true,
            generalError = null
        )

        viewModelScope.launch {
            try {
                accountSessionCoordinator.continueAsGuest()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isCompleted = true,
                    password = ""
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = "Не удалось открыть гостевой профиль."
                )
            }
        }
    }

    fun onCompletionHandled() {
        _uiState.value = _uiState.value.copy(
            isCompleted = false
        )
    }

    // Factory - это объект, который отвечает за создание экземпляров других классов
    // нужна для создания LoginViewModel
    companion object { // объект, связанный с самим классом, а не с его отдельным экземпляром
        // интерфейс AndroidX, описывающий механизм создания ViewModel
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            // API для реализации фабрики без написания отдельного класса
            initializer {
                val application = this[
                    // ключ, по которому из CreationExtras получаем экземпляр Android Application
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CodonautApplication

                LoginViewModel(
                    accountSessionCoordinator =
                        application.container.accountSessionCoordinator
                )
            }
        }
    }
}
