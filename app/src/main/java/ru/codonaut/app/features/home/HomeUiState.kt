package ru.codonaut.app.features.home

import ru.codonaut.app.domain.model.ProgrammingLanguage

data class HomeUiState(
    val isLoading: Boolean = true,
    val profileId: String? = null,
    val selectedLanguages: List<ProgrammingLanguage> = emptyList(),
    val totalXp: Long = 0,
    val levelTitle: String? = null,
    val levelProgress: Float? = null,
    val levelProgressLabel: String? = null,
    val currentLesson: CurrentLessonUiModel? = null,
    val recommendation: RecommendationUiModel? = null
)

data class CurrentLessonUiModel(
    val language: ProgrammingLanguage,
    val lessonNumber: Int,
    val topic: String,
    val lessonId: String
)

data class RecommendationUiModel(
    val topic: String,
    val description: String
)
