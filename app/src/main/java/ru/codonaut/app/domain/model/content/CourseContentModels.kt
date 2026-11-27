package ru.codonaut.app.domain.model.content

// Проектные доменные модели для учебного контента.
// Преобразование JSON -> эти модели выполняет отдельный загрузчик.
// Файл не требует дополнительных зависимостей.

data class CourseContent(
    val schemaVersion: Int,
    val languageId: String,
    val title: String,
    val lessons: List<LessonContent>
)

data class LessonContent(
    val id: String,
    val order: Int,
    val title: String,
    val objective: String,
    val theoryBlocks: List<TheoryBlockContent>,
    val tasks: List<TaskContent>
)

data class TheoryBlockContent(
    val id: String,
    val order: Int,
    val title: String,
    val text: String,
    val codeExample: String?,
    val expectedOutput: String?,
    val rememberNote: String? = null,
    val infoCard: TheoryInfoCardContent? = null,
    val exampleExplanation: String? = null
)

data class TheoryInfoCardContent(
    val title: String,
    val text: String,
    val detail: String? = null
)

enum class TaskType {
    SINGLE_CHOICE,
    OUTPUT_PREDICTION,
    CODE_COMPLETION,
    ORDER_STEPS
}

enum class TaskRole {
    REQUIRED,
    REINFORCEMENT
}

enum class CodeComparisonMode {
    TRIM_OUTER_WHITESPACE
}

data class AnswerOptionContent(
    val id: String,
    val text: String
)

data class StepContent(
    val id: String,
    val text: String
)

sealed interface TaskContent {
    val id: String
    val order: Int
    val role: TaskRole
    val question: String
    val correctExplanation: String
    val incorrectExplanation: String
    val type: TaskType
}

data class SingleChoiceTaskContent(
    override val id: String,
    override val order: Int,
    override val role: TaskRole,
    override val question: String,
    override val correctExplanation: String,
    override val incorrectExplanation: String,
    val options: List<AnswerOptionContent>,
    val correctOptionId: String
) : TaskContent {
    override val type: TaskType = TaskType.SINGLE_CHOICE
}

data class OutputPredictionTaskContent(
    override val id: String,
    override val order: Int,
    override val role: TaskRole,
    override val question: String,
    override val correctExplanation: String,
    override val incorrectExplanation: String,
    val codeSnippet: String,
    val options: List<AnswerOptionContent>,
    val correctOptionId: String
) : TaskContent {
    override val type: TaskType = TaskType.OUTPUT_PREDICTION
}

data class CodeCompletionTaskContent(
    override val id: String,
    override val order: Int,
    override val role: TaskRole,
    override val question: String,
    override val correctExplanation: String,
    override val incorrectExplanation: String,
    val codeTemplate: String,
    val placeholder: String?,
    val acceptedAnswers: List<String>,
    val comparisonMode: CodeComparisonMode
) : TaskContent {
    override val type: TaskType = TaskType.CODE_COMPLETION
}

data class OrderStepsTaskContent(
    override val id: String,
    override val order: Int,
    override val role: TaskRole,
    override val question: String,
    override val correctExplanation: String,
    override val incorrectExplanation: String,
    val steps: List<StepContent>,
    val correctOrder: List<String>
) : TaskContent {
    override val type: TaskType = TaskType.ORDER_STEPS
}
