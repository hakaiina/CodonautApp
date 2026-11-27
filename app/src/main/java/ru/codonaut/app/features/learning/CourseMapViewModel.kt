package ru.codonaut.app.features.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import ru.codonaut.app.CodonautApplication
import ru.codonaut.app.data.local.entity.LessonProgressEntity
import ru.codonaut.app.data.repository.AssetCourseContentRepository
import ru.codonaut.app.domain.model.content.LessonContent
import ru.codonaut.app.domain.repository.SessionRepository

data class CourseMapUiState(
    val lessons: List<LessonContent> = emptyList(),
    val completedLessonIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class CourseMapViewModel(
    languageId: String,
    sessions: SessionRepository,
    private val courses: AssetCourseContentRepository,
    private val progress: ru.codonaut.app.data.local.dao.LessonProgressDao
) : ViewModel() {
    private val content = flow {
        emit(withContext(Dispatchers.IO) { courses.getLessons(languageId) })
    }

    val uiState = sessions.activeProfileId.flatMapLatest { profileId ->
        if (profileId.isNullOrBlank()) content.map { CourseMapUiState(it, emptySet(), false) }
        else kotlinx.coroutines.flow.combine(content, progress.observeForProfile(profileId)) { lessons, rows ->
            CourseMapUiState(lessons, rows.filter { it.completedAt != null }.map { it.lessonId }.toSet(), false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseMapUiState())

    companion object {
        fun factory(languageId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CodonautApplication
                CourseMapViewModel(languageId, app.container.sessionRepository,
                    app.container.courseContentRepository, app.container.database.lessonProgressDao())
            }
        }
    }
}
