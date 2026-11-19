package ru.codonaut.app.features.languageSelection

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
import ru.codonaut.app.R
import ru.codonaut.app.domain.model.ProgrammingLanguage
import ru.codonaut.app.domain.repository.SelectedLanguageRepository
import ru.codonaut.app.domain.repository.SessionRepository

class LanguageSelectionViewModel(
    private val sessionRepository: SessionRepository,
    private val selectedLanguageRepository: SelectedLanguageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageSelectionUiState())
    val uiState = _uiState.asStateFlow()
    private var activeProfileId: String? = null
    private var completed = false

    init {
        loadSelection()
    }

    private fun loadSelection() {
        viewModelScope.launch {
            try {
                val profile = sessionRepository.getActiveProfile()
                    ?: error("No active local profile")
                activeProfileId = profile.profileId
                selectedLanguageRepository
                    .observeSelectedLanguageIds(profile.profileId)
                    .collect { ids ->
                        _uiState.value = _uiState.value.copy(
                            selectedLanguageIds = ids
                                .filter { ProgrammingLanguage.fromId(it) != null }
                                .toSet(),
                            isLoading = false,
                            errorMessageRes = null
                        )
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessageRes = R.string.language_selection_load_error
                )
            }
        }
    }

    fun toggleLanguage(language: ProgrammingLanguage) {
        val current = _uiState.value
        if (current.isLoading || current.isSaving || completed) return
        val selection = current.selectedLanguageIds.toMutableSet()
        if (!selection.add(language.id)) selection.remove(language.id)
        _uiState.value = current.copy(
            selectedLanguageIds = selection,
            errorMessageRes = null
        )
    }

    fun saveSelection(onSaved: () -> Unit) {
        val current = _uiState.value
        val profileId = activeProfileId
        if (!current.canContinue || profileId == null || completed) return

        _uiState.value = current.copy(isSaving = true, errorMessageRes = null)
        viewModelScope.launch {
            try {
                selectedLanguageRepository.saveSelectedLanguageIds(
                    profileId = profileId,
                    languageIds = current.selectedLanguageIds
                )
                completed = true
                _uiState.value = _uiState.value.copy(isSaving = false)
                onSaved()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessageRes = R.string.language_selection_save_error
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
                LanguageSelectionViewModel(
                    sessionRepository = application.container.sessionRepository,
                    selectedLanguageRepository =
                        application.container.selectedLanguageRepository
                )
            }
        }
    }
}
