package ru.codonaut.app.features.debug

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
import ru.codonaut.app.domain.model.UserProfile
import ru.codonaut.app.domain.repository.SessionRepository

data class GuestSessionUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val error: String? = null
)

class GuestSessionViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GuestSessionUiState()
    )

    val uiState = _uiState.asStateFlow()

    init {
        restoreProfile()
    }

    private fun restoreProfile() {
        viewModelScope.launch {
            try {
                val profile = sessionRepository.getActiveProfile()

                _uiState.value = GuestSessionUiState(
                    isLoading = false,
                    profile = profile
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = GuestSessionUiState(
                    isLoading = false,
                    error = e.message ?: "Не удалось загрузить профиль"
                )
            }
        }
    }

    fun continueAsGuest() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val profile = sessionRepository.continueAsGuest()

                _uiState.value = GuestSessionUiState(
                    isLoading = false,
                    profile = profile
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = GuestSessionUiState(
                    isLoading = false,
                    error = e.message ?: "Не удалось создать профиль"
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CodonautApplication

                GuestSessionViewModel(
                    application.container.sessionRepository
                )
            }
        }
    }
}
