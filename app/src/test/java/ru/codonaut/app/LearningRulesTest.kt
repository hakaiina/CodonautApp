package ru.codonaut.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.codonaut.app.domain.model.content.*
import ru.codonaut.app.domain.usecase.LearningProgressRules
import ru.codonaut.app.domain.usecase.LessonAccessRules
import ru.codonaut.app.domain.usecase.TaskAnswerChecker

class LearningRulesTest {
    @Test fun checkerHandlesAllTaskTypes() {
        val single = SingleChoiceTaskContent("s", 1, TaskRole.REQUIRED, "?", "yes", "no",
            listOf(AnswerOptionContent("a", "A"), AnswerOptionContent("b", "B")), "b")
        val output = OutputPredictionTaskContent("o", 2, TaskRole.REQUIRED, "?", "yes", "no", "print(1)",
            listOf(AnswerOptionContent("1", "1")), "1")
        val completion = CodeCompletionTaskContent("c", 3, TaskRole.REQUIRED, "?", "yes", "no", "print(____)",
            null, listOf("\"OK\"", "'OK'"), CodeComparisonMode.TRIM_OUTER_WHITESPACE)
        val order = OrderStepsTaskContent("p", 4, TaskRole.REQUIRED, "?", "yes", "no",
            listOf(StepContent("a", "A"), StepContent("b", "B")), listOf("b", "a"))
        assertTrue(TaskAnswerChecker.check(single, "b")); assertFalse(TaskAnswerChecker.check(single, "a"))
        assertTrue(TaskAnswerChecker.check(output, "1")); assertFalse(TaskAnswerChecker.check(output, "2"))
        assertTrue(TaskAnswerChecker.check(completion, "  'OK'  ")); assertFalse(TaskAnswerChecker.check(completion, "'ok'"))
        assertTrue(TaskAnswerChecker.check(order, "b,a")); assertFalse(TaskAnswerChecker.check(order, "a,b"))
    }

    @Test fun xpAwardsOnlyForFirstCompletionAndLevelUses150XpBands() {
        assertEquals(50, LearningProgressRules.completionReward(true))
        assertEquals(0, LearningProgressRules.completionReward(false))
        assertEquals(1L, LearningProgressRules.level(149).level)
        assertEquals(2L, LearningProgressRules.level(150).level)
        assertEquals(0.5f, LearningProgressRules.level(225).progress)
    }

    @Test fun onlyFirstIncompleteLessonIsAvailable() {
        val lessons = listOf(lesson("l1", 1), lesson("l2", 2), lesson("l3", 3))
        assertEquals("l1", LessonAccessRules.firstAvailable(lessons, emptySet())?.id)
        assertNull(LessonAccessRules.firstAvailable(lessons, setOf("l1", "l2", "l3")))
        assertEquals("l2", LessonAccessRules.firstAvailable(lessons, setOf("l1"))?.id)
        assertFalse(LessonAccessRules.isAccessible(lessons[2], lessons, emptySet()))
        assertTrue(LessonAccessRules.isAccessible(lessons[0], lessons, setOf("l1")))
    }

    private fun lesson(id: String, order: Int) = LessonContent(
        id, order, "Урок $order", "Цель", emptyList(), emptyList()
    )
}
