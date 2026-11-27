package ru.codonaut.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import ru.codonaut.app.CodonautApplication
import ru.codonaut.app.domain.model.ProgrammingLanguage
import ru.codonaut.app.domain.usecase.LessonAccessRules
import ru.codonaut.app.domain.usecase.LearningProgressRules

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    activeProfileId: kotlinx.coroutines.flow.Flow<String?>,
    private val profileRepository: ru.codonaut.app.domain.repository.ProfileRepository,
    private val selectedLanguageRepository: ru.codonaut.app.domain.repository.SelectedLanguageRepository,
    private val lessonProgressDao: ru.codonaut.app.data.local.dao.LessonProgressDao,
    private val courseRepository: ru.codonaut.app.data.repository.AssetCourseContentRepository
) : ViewModel() {

    val uiState = activeProfileId
        .flatMapLatest { profileId ->
            if (profileId.isNullOrBlank()) {
                flowOf(HomeUiState(isLoading = false))
            } else {
                combine(
                    profileRepository.observeById(profileId),
                    selectedLanguageRepository.observeSelectedLanguageIds(profileId),
                    lessonProgressDao.observeTotalXp(profileId),
                    lessonProgressDao.observeForProfile(profileId)
                ) { profile, languageIds, totalXp, progress ->
                    HomeInputs(
                        HomeUiState(
                            isLoading = false,
                            profileId = profile?.profileId,
                            selectedLanguages = languageIds.mapNotNull(ProgrammingLanguage::fromId),
                            totalXp = totalXp.coerceAtLeast(0)
                        ),
                        progress
                    )
                }.mapLatest { inputs ->
                    val base = inputs.state
                    val completedIds = inputs.progress.filter { it.completedAt != null }.map { it.lessonId }.toSet()
                    val selectedIds = base.selectedLanguages
                    val lesson = withContext(Dispatchers.IO) {
                        selectedIds.asSequence().mapNotNull { language ->
                            val lessons = courseRepository.getLessons(language.id)
                            val next = LessonAccessRules.firstAvailable(lessons, completedIds)
                            next?.let { language to it }
                        }.firstOrNull()
                    }
                    val level = LearningProgressRules.level(base.totalXp)
                    base.copy(
                        currentLesson = lesson?.let { (language, content) ->
                            CurrentLessonUiModel(language, content.order, content.title, content.id)
                        },
                        levelTitle = "Уровень ${level.level}",
                        levelProgress = level.progress,
                        levelProgressLabel = "${level.xpIntoLevel} / 150 XP до следующего уровня"
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as CodonautApplication
                val container = application.container
                HomeViewModel(
                    activeProfileId = container.sessionRepository.activeProfileId,
                    profileRepository = container.profileRepository,
                    selectedLanguageRepository = container.selectedLanguageRepository,
                    lessonProgressDao = container.database.lessonProgressDao(),
                    courseRepository = container.courseContentRepository
                )
            }
        }
    }

    private data class HomeInputs(
        val state: HomeUiState,
        val progress: List<ru.codonaut.app.data.local.entity.LessonProgressEntity>
    )
}
