package ru.codonaut.app.features.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.codonaut.app.CodonautApplication
import ru.codonaut.app.data.local.entity.LessonRunEntity
import ru.codonaut.app.data.repository.LocalLearningRepository
import ru.codonaut.app.domain.model.content.LessonContent
import ru.codonaut.app.domain.model.content.TaskContent
import ru.codonaut.app.domain.model.content.TaskRole
import ru.codonaut.app.domain.usecase.TaskAnswerChecker

data class LessonUiState(
    val lesson: LessonContent? = null,
    val run: LessonRunEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val answerCorrect: Boolean? = null,
    val answeredTask: TaskContent? = null,
    val explanation: String? = null,
    val xpAwarded: Int = 0,
    val isSubmitting: Boolean = false
)

class LessonViewModel(
    private val languageId: String,
    private val lessonId: String,
    private val sessions: ru.codonaut.app.domain.repository.SessionRepository,
    private val courses: ru.codonaut.app.data.repository.AssetCourseContentRepository,
    private val learning: LocalLearningRepository
) : ViewModel() {
    private val mutableState = MutableStateFlow(LessonUiState())
    val uiState = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val profileId = sessions.activeProfileId.first()?.takeIf(String::isNotBlank)
                    ?: error("Не найден активный профиль")
                val lesson = withContext(Dispatchers.IO) { courses.getLesson(languageId, lessonId) }
                    ?: error("Урок недоступен")
                val savedRun = learning.startOrResume(profileId, lessonId)
                val run = if (savedRun.reinforcementPending) learning.continueAfterFeedback(savedRun) else savedRun
                LessonUiState(lesson = lesson, run = run, isLoading = false)
            }.onSuccess { mutableState.value = it }
                .onFailure { mutableState.value = LessonUiState(isLoading = false, error = "Не удалось открыть урок: ${it.message ?: "ошибка данных"}") }
        }
    }

    val currentTask: TaskContent?
        get() {
            val state = mutableState.value
            val lesson = state.lesson ?: return null
            val run = state.run ?: return null
            return state.answeredTask ?: if (run.phase == "REINFORCEMENT") lesson.tasks.firstOrNull { it.role == TaskRole.REINFORCEMENT }
            else lesson.tasks.filter { it.role == TaskRole.REQUIRED }.sortedBy { it.order }.getOrNull(run.taskOrder)
        }

    fun nextTheory() {
        val state = mutableState.value
        val lesson = state.lesson ?: return
        val run = state.run ?: return
        viewModelScope.launch {
            val nextIndex = run.theoryIndex + 1
            val updated = if (nextIndex >= lesson.theoryBlocks.size) learning.finishTheory(run)
            else learning.saveTheory(run, nextIndex)
            mutableState.value = state.copy(run = updated)
        }
    }

    fun submitAnswer(answer: String) {
        val state = mutableState.value
        val task = currentTask ?: return
        val run = state.run ?: return
        if (state.answerCorrect != null || state.isSubmitting) return
        mutableState.value = state.copy(isSubmitting = true)
        viewModelScope.launch {
            try {
                val correct = TaskAnswerChecker.check(task, answer)
                val reinforcement = run.phase == "REINFORCEMENT"
                val result = learning.submitAnswer(run, task.id, correct, reinforcement,
                    state.lesson!!.tasks.count { it.role == TaskRole.REQUIRED })
                mutableState.value = state.copy(run = result.run, answerCorrect = correct, answeredTask = task,
                    explanation = if (correct) task.correctExplanation else task.incorrectExplanation,
                    xpAwarded = result.xpAwarded, isSubmitting = false)
            } catch (_: Exception) {
                mutableState.value = state.copy(isSubmitting = false,
                    explanation = "Не удалось сохранить ответ. Попробуй ещё раз.")
            }
        }
    }

    fun continueAfterAnswer() {
        val state = mutableState.value
        val run = state.run ?: return
        viewModelScope.launch {
            val updated = learning.continueAfterFeedback(run)
            mutableState.value = state.copy(run = updated, answerCorrect = null,
                answeredTask = null, explanation = null, xpAwarded = 0, isSubmitting = false)
        }
    }

    companion object {
        fun factory(languageId: String, lessonId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CodonautApplication
                LessonViewModel(languageId, lessonId, app.container.sessionRepository,
                    app.container.courseContentRepository, app.container.learningRepository)
            }
        }
    }
}
