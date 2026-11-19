package ru.codonaut.app.features.languageSelection

data class LanguageSelectionUiState(
    val selectedLanguageIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessageRes: Int? = null
) {
    val canContinue: Boolean
        get() = selectedLanguageIds.isNotEmpty() && !isLoading && !isSaving
}
