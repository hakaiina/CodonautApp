package ru.codonaut.app.data.repository

import androidx.room.withTransaction
import ru.codonaut.app.data.local.CodonautDatabase
import ru.codonaut.app.data.local.entity.LessonProgressEntity
import ru.codonaut.app.data.local.entity.LessonRunEntity
import ru.codonaut.app.data.local.entity.TaskAttemptEntity
import java.util.UUID
import ru.codonaut.app.domain.usecase.LearningProgressRules

data class AnswerSaveResult(val run: LessonRunEntity, val xpAwarded: Int)

class LocalLearningRepository(private val database: CodonautDatabase) {
    private val progress get() = database.lessonProgressDao()
    private val runs get() = database.lessonRunDao()
    private val attempts get() = database.taskAttemptDao()

    suspend fun startOrResume(profileId: String, lessonId: String): LessonRunEntity = database.withTransaction {
        val progressRow = progress.getByLessonId(profileId, lessonId)
        if (progressRow?.completedAt == null) {
            runs.getActive(profileId, lessonId)?.let { return@withTransaction it }
        }
        LessonRunEntity(
            runId = UUID.randomUUID().toString(), profileId = profileId,
            lessonId = lessonId, createdAt = System.currentTimeMillis()
        ).also { runs.insert(it) }
    }

    suspend fun saveTheory(run: LessonRunEntity, index: Int): LessonRunEntity = run.copy(
        phase = "THEORY", theoryIndex = index.coerceAtLeast(0)
    ).also { runs.update(it) }

    suspend fun finishTheory(run: LessonRunEntity): LessonRunEntity = run.copy(phase = "TASK").also { runs.update(it) }

    suspend fun submitAnswer(
        run: LessonRunEntity,
        taskId: String,
        correct: Boolean,
        isReinforcement: Boolean,
        requiredTaskCount: Int
    ): AnswerSaveResult = database.withTransaction {
        val persistedRun = runs.getById(run.runId) ?: return@withTransaction AnswerSaveResult(run, 0)
        if (persistedRun != run) return@withTransaction AnswerSaveResult(persistedRun, 0)
        attempts.insert(
            TaskAttemptEntity(
                attemptId = UUID.randomUUID().toString(), profileId = run.profileId,
                taskId = taskId, isCorrect = correct, answeredAt = System.currentTimeMillis(),
                runId = run.runId, isReinforcement = isReinforcement
            )
        )
        if (isReinforcement) {
            val updated = run.copy(phase = "TASK", reinforcementPending = false).also { runs.update(it) }
            return@withTransaction AnswerSaveResult(updated, 0)
        }

        if (!correct) {
            val misses = run.wrongAttempts + 1
            val pending = misses >= 2 && !run.reinforcementOffered
            val updated = run.copy(
                wrongAttempts = misses,
                reinforcementPending = pending
            ).also { runs.update(it) }
            return@withTransaction AnswerSaveResult(updated, 0)
        }

        val nextOrder = run.taskOrder + 1
        if (nextOrder < requiredTaskCount) {
            val updated = run.copy(taskOrder = nextOrder, wrongAttempts = 0, reinforcementPending = false)
                .also { runs.update(it) }
            return@withTransaction AnswerSaveResult(updated, 0)
        }

        progress.insertIfAbsent(LessonProgressEntity(run.profileId, run.lessonId))
        val newlyCompleted = progress.completeIfNotCompleted(
            profileId = run.profileId, lessonId = run.lessonId,
            completedAt = System.currentTimeMillis(), xpEarned = 50
        ) == 1
        val updated = run.copy(phase = "COMPLETE", completedAt = System.currentTimeMillis())
            .also { runs.update(it) }
        AnswerSaveResult(updated, LearningProgressRules.completionReward(newlyCompleted))
    }

    suspend fun continueAfterFeedback(run: LessonRunEntity): LessonRunEntity {
        if (!run.reinforcementPending) return run
        return run.copy(
            phase = "REINFORCEMENT", reinforcementPending = false,
            reinforcementOffered = true
        ).also { runs.update(it) }
    }
}
