package ru.codonaut.app.data.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import ru.codonaut.app.domain.model.content.*

/** Loads and validates bundled course files without any dependency on Compose. */
class AssetCourseContentRepository(context: Context) {
    private val assets = context.applicationContext.assets

    fun getLessons(languageId: String): List<LessonContent> = runCatching {
        val root = assets.open("courses/$languageId.json").bufferedReader().use { JSONObject(it.readText()) }
        require(root.getInt("schemaVersion") > 0)
        require(root.getString("languageId") == languageId)
        require(root.getString("title").isNotBlank())
        val lessons = root.getJSONArray("lessons").mapObjects { it.toLesson() }
        requireUnique(lessons.map { it.id })
        require(lessons.all { it.order > 0 } && lessons.map { it.order }.distinct().size == lessons.size)
        requireUnique(lessons.flatMap { lesson -> lesson.theoryBlocks.map { it.id } })
        requireUnique(lessons.flatMap { lesson -> lesson.tasks.map { it.id } })
        lessons.forEach { lesson ->
            require(lesson.id.isNotBlank() && lesson.title.isNotBlank() && lesson.objective.isNotBlank())
            require(lesson.theoryBlocks.all { it.order > 0 } && lesson.theoryBlocks.map { it.order }.distinct().size == lesson.theoryBlocks.size)
            require(lesson.tasks.all { it.order > 0 } && lesson.tasks.map { it.order }.distinct().size == lesson.tasks.size)
            require(lesson.theoryBlocks.isNotEmpty())
            require(lesson.tasks.count { it.role == TaskRole.REQUIRED } == 4)
            require(lesson.tasks.count { it.role == TaskRole.REINFORCEMENT } == 1)
            requireUnique(lesson.theoryBlocks.map { it.id })
            requireUnique(lesson.tasks.map { it.id })
            lesson.theoryBlocks.forEach { require(it.title.isNotBlank() && it.text.isNotBlank()) }
            lesson.tasks.forEach(::validateTask)
        }
        lessons.sortedBy { it.order }
    }.getOrElse { emptyList() }

    fun getLesson(languageId: String, lessonId: String): LessonContent? =
        getLessons(languageId).firstOrNull { it.id == lessonId }

    private fun validateTask(task: TaskContent) {
        require(task.id.isNotBlank() && task.question.isNotBlank())
        require(task.correctExplanation.isNotBlank() && task.incorrectExplanation.isNotBlank())
        when (task) {
            is SingleChoiceTaskContent -> {
                requireUnique(task.options.map { it.id }); require(task.options.isNotEmpty())
                require(task.options.all { it.text.isNotBlank() })
                require(task.options.any { it.id == task.correctOptionId })
            }
            is OutputPredictionTaskContent -> {
                require(task.codeSnippet.isNotBlank()); requireUnique(task.options.map { it.id })
                require(task.options.all { it.text.isNotBlank() })
                require(task.options.isNotEmpty() && task.options.any { it.id == task.correctOptionId })
            }
            is CodeCompletionTaskContent -> {
                require(task.codeTemplate.isNotBlank() && task.acceptedAnswers.isNotEmpty() && task.acceptedAnswers.all(String::isNotBlank))
            }
            is OrderStepsTaskContent -> {
                requireUnique(task.steps.map { it.id }); require(task.steps.isNotEmpty())
                require(task.steps.all { it.text.isNotBlank() })
                require(task.correctOrder.toSet() == task.steps.map { it.id }.toSet())
            }
        }
    }

    private fun requireUnique(ids: List<String>) {
        require(ids.none { it.isBlank() } && ids.size == ids.toSet().size)
    }

    private fun JSONArray.mapObjects(transform: (JSONObject) -> LessonContent): List<LessonContent> =
        (0 until length()).map { transform(getJSONObject(it)) }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    private fun JSONObject.toLesson() = LessonContent(
        id = getString("id"), order = getInt("order"), title = getString("title"),
        objective = getString("objective"),
        theoryBlocks = getJSONArray("theoryBlocks").objects().map {
            TheoryBlockContent(it.getString("id"), it.getInt("order"), it.getString("title"),
                it.getString("text"), it.optString("codeExample").takeIf(String::isNotEmpty),
                it.optString("expectedOutput").takeIf(String::isNotEmpty),
                it.optString("rememberNote").takeIf(String::isNotEmpty),
                it.optJSONObject("infoCard")?.let { info ->
                    TheoryInfoCardContent(
                        title = info.optString("title"),
                        text = info.optString("text"),
                        detail = info.optString("detail").takeIf(String::isNotEmpty)
                    ).takeIf { card -> card.title.isNotBlank() && card.text.isNotBlank() }
                },
                it.optString("exampleExplanation").takeIf(String::isNotEmpty))
        }.sortedBy { it.order },
        tasks = getJSONArray("tasks").objects().map { it.toTask() }.sortedBy { it.order }
    )

    private fun JSONObject.toTask(): TaskContent {
        val common = arrayOf(getString("id"), getInt("order"), TaskRole.valueOf(getString("role")),
            getString("question"), getString("correctExplanation"), getString("incorrectExplanation"))
        return when (TaskType.valueOf(getString("type"))) {
            TaskType.SINGLE_CHOICE -> SingleChoiceTaskContent(
                common[0] as String, common[1] as Int, common[2] as TaskRole, common[3] as String,
                common[4] as String, common[5] as String, getJSONArray("options").objects().map {
                    AnswerOptionContent(it.getString("id"), it.getString("text"))
                }, getString("correctOptionId"))
            TaskType.OUTPUT_PREDICTION -> OutputPredictionTaskContent(
                common[0] as String, common[1] as Int, common[2] as TaskRole, common[3] as String,
                common[4] as String, common[5] as String, getString("codeSnippet"),
                getJSONArray("options").objects().map { AnswerOptionContent(it.getString("id"), it.getString("text")) },
                getString("correctOptionId"))
            TaskType.CODE_COMPLETION -> CodeCompletionTaskContent(
                common[0] as String, common[1] as Int, common[2] as TaskRole, common[3] as String,
                common[4] as String, common[5] as String, getString("codeTemplate"),
                optString("placeholder").takeIf(String::isNotEmpty),
                getJSONArray("acceptedAnswers").let { a -> (0 until a.length()).map(a::getString) },
                CodeComparisonMode.valueOf(getString("comparisonMode")))
            TaskType.ORDER_STEPS -> OrderStepsTaskContent(
                common[0] as String, common[1] as Int, common[2] as TaskRole, common[3] as String,
                common[4] as String, common[5] as String,
                getJSONArray("steps").objects().map { StepContent(it.getString("id"), it.getString("text")) },
                getJSONArray("correctOrder").let { a -> (0 until a.length()).map(a::getString) })
        }
    }
}
