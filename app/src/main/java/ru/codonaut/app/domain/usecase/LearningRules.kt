package ru.codonaut.app.domain.usecase

import ru.codonaut.app.domain.model.content.LessonContent

object LessonAccessRules {
    fun firstAvailable(lessons: List<LessonContent>, completedIds: Set<String>): LessonContent? =
        lessons.sortedBy { it.order }.firstOrNull { candidate ->
            candidate.id !in completedIds && lessons.filter { it.order < candidate.order }.all { it.id in completedIds }
        }

    fun isAccessible(lesson: LessonContent, lessons: List<LessonContent>, completedIds: Set<String>): Boolean =
        lesson.id in completedIds || firstAvailable(lessons, completedIds)?.id == lesson.id
}

data class UserLevel(val level: Long, val xpIntoLevel: Long, val progress: Float)

object LearningProgressRules {
    fun level(totalXp: Long): UserLevel {
        val xp = totalXp.coerceAtLeast(0)
        val within = xp % 150
        return UserLevel(level = xp / 150 + 1, xpIntoLevel = within, progress = within / 150f)
    }

    fun completionReward(isFirstCompletion: Boolean): Int = if (isFirstCompletion) 50 else 0
}
