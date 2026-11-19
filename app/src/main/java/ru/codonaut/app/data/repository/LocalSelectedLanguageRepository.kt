package ru.codonaut.app.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import ru.codonaut.app.data.local.CodonautDatabase
import ru.codonaut.app.data.local.dao.SelectedLanguageDao
import ru.codonaut.app.data.local.entity.SelectedLanguageEntity
import ru.codonaut.app.domain.model.ProgrammingLanguage
import ru.codonaut.app.domain.repository.SelectedLanguageRepository

class LocalSelectedLanguageRepository(
    private val database: CodonautDatabase,
    private val dao: SelectedLanguageDao
) : SelectedLanguageRepository {

    override fun observeSelectedLanguageIds(profileId: String): Flow<List<String>> =
        dao.observeSelectedLanguageIds(profileId)

    override suspend fun getSelectedLanguageIds(profileId: String): List<String> =
        observeSelectedLanguageIds(profileId).first()

    override suspend fun saveSelectedLanguageIds(
        profileId: String,
        languageIds: Set<String>
    ) {
        require(profileId.isNotBlank()) { "Profile ID must not be blank" }
        require(languageIds.all { id -> ProgrammingLanguage.fromId(id) != null }) {
            "Unknown programming language ID"
        }

        database.withTransaction {
            dao.deleteForProfile(profileId)
            dao.insertAll(
                languageIds.sorted().map { languageId ->
                    SelectedLanguageEntity(
                        profileId = profileId,
                        languageId = languageId
                    )
                }
            )
        }
    }
}
