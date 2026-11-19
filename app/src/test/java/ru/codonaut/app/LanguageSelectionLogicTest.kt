package ru.codonaut.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.codonaut.app.domain.model.ProgrammingLanguage
import ru.codonaut.app.features.languageSelection.LanguageSelectionUiState

class LanguageSelectionLogicTest {

    @Test
    fun stableLanguageIdsMapToExpectedLanguages() {
        assertEquals(ProgrammingLanguage.PYTHON, ProgrammingLanguage.fromId("python"))
        assertEquals(ProgrammingLanguage.JAVASCRIPT, ProgrammingLanguage.fromId("javascript"))
        assertEquals(ProgrammingLanguage.JAVA, ProgrammingLanguage.fromId("java"))
        assertNull(ProgrammingLanguage.fromId("Python"))
    }

    @Test
    fun continueIsOnlyEnabledWithSelectionAndWhenIdle() {
        assertFalse(LanguageSelectionUiState(isLoading = false).canContinue)
        assertTrue(
            LanguageSelectionUiState(
                selectedLanguageIds = setOf("python"),
                isLoading = false
            ).canContinue
        )
        assertFalse(
            LanguageSelectionUiState(
                selectedLanguageIds = setOf("python"),
                isLoading = false,
                isSaving = true
            ).canContinue
        )
    }
}
