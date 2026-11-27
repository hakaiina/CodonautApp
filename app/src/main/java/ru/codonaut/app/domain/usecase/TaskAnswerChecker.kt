package ru.codonaut.app.domain.usecase

import ru.codonaut.app.domain.model.content.*

/** Pure answer validation. No user supplied source code is executed. */
object TaskAnswerChecker {
    fun check(task: TaskContent, answer: String): Boolean = when (task) {
        is SingleChoiceTaskContent -> answer == task.correctOptionId
        is OutputPredictionTaskContent -> answer == task.correctOptionId
        is CodeCompletionTaskContent -> task.acceptedAnswers.any { it == answer.trim() }
        is OrderStepsTaskContent -> answer.split(',') == task.correctOrder
    }
}
