package ru.codonaut.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SelectedLanguageRepository {
    fun observeSelectedLanguageIds(profileId: String): Flow<List<String>>

    suspend fun getSelectedLanguageIds(profileId: String): List<String>

    suspend fun saveSelectedLanguageIds(
        profileId: String,
        languageIds: Set<String>
    )
}
