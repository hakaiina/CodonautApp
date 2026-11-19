package ru.codonaut.app.features.startup

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
import ru.codonaut.app.domain.repository.AppStartupRepository
import ru.codonaut.app.domain.repository.SelectedLanguageRepository
import ru.codonaut.app.domain.usecase.AccountSessionCoordinator

// ViewModel, которая определяет, какой экран нужно открыть при запуске приложения

enum class StartupUiState {
    Loading,
    Onboarding,
    Login,
    LanguageSelection,
    Session,
    Error
}

class StartupViewModel(
    private val coordinator: AccountSessionCoordinator,
    private val appStartupRepository: AppStartupRepository,
    private val selectedLanguageRepository: SelectedLanguageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StartupUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private var restoreJobStarted = false

    init {
        restore()
    }

    fun restore() {
        if (_uiState.value == StartupUiState.Loading &&
            restoreJobStarted
        ) return

        restoreJobStarted = true
        _uiState.value = StartupUiState.Loading

        loadDestination()
    }

    /** Re-checks the destination after login, registration, or guest activation. */
    fun refreshSessionDestination() {
        restoreJobStarted = true
        _uiState.value = StartupUiState.Loading
        loadDestination()
    }

    private fun loadDestination() {
        viewModelScope.launch {
            try {
                val profile = coordinator.restoreSession()

                _uiState.value = when {
                    profile == null && appStartupRepository.isOnboardingCompleted() ->
                        StartupUiState.Login

                    profile == null -> StartupUiState.Onboarding

                    selectedLanguageRepository
                        .getSelectedLanguageIds(profile.profileId).isEmpty() ->
                        StartupUiState.LanguageSelection

                    else -> StartupUiState.Session
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = StartupUiState.Error
            }
        }
    }

    fun completeOnboarding() {
        if (_uiState.value != StartupUiState.Onboarding) return

        _uiState.value = StartupUiState.Loading

        viewModelScope.launch {
            try {
                appStartupRepository.completeOnboarding()
                _uiState.value = StartupUiState.Login
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = StartupUiState.Error
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CodonautApplication

                StartupViewModel(
                    coordinator =
                        application.container.accountSessionCoordinator,
                    appStartupRepository =
                        application.container.appStartupRepository,
                    selectedLanguageRepository =
                        application.container.selectedLanguageRepository
                )
            }
        }
    }
}
