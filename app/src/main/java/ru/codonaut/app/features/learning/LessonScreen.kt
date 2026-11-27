package ru.codonaut.app.features.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.codonaut.app.R
import ru.codonaut.app.core.ui.components.CodonautBackButton
import ru.codonaut.app.core.ui.theme.CodonautCodeStyle
import ru.codonaut.app.core.ui.theme.CodonautColors
import ru.codonaut.app.core.ui.theme.JetBrainsMono
import ru.codonaut.app.core.ui.theme.Rubik
import ru.codonaut.app.domain.model.ProgrammingLanguage
import ru.codonaut.app.domain.model.content.*

@Composable
fun LessonScreen(
    languageId: String,
    lessonId: String,
    onBack: () -> Unit,
    viewModel: LessonViewModel = viewModel(
        key = "lesson-$languageId-$lessonId",
        factory = LessonViewModel.factory(languageId, lessonId)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lesson = state.lesson
    val run = state.run
    var answer by remember { mutableStateOf("") }
    var order by remember { mutableStateOf(emptyList<String>()) }
    val task = viewModel.currentTask

    LaunchedEffect(state.answeredTask, task?.id, run?.phase) {
        if (state.answeredTask == null) answer = ""
        if (task is OrderStepsTaskContent) order = task.steps.shuffled().map { it.id }
    }

    Column(
        Modifier.fillMaxSize()
            .background(CodonautColors.WarmMist)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding()
    ) {
        if (lesson != null && run != null && (run.phase != "COMPLETE" || state.answerCorrect != null)) {
            val isTheory = run.phase == "THEORY"
            val steps = if (isTheory) lesson.theoryBlocks.size
                else lesson.tasks.count { it.role == TaskRole.REQUIRED }
            val step = if (isTheory) run.theoryIndex + 1
                else if (run.phase == "REINFORCEMENT") 1 else run.taskOrder + 1
            LessonTopBar(
                title = "${if (isTheory) "Теория" else if (run.phase == "REINFORCEMENT") "Закрепление" else "Практика"} · " +
                    "${ProgrammingLanguage.fromId(languageId)?.displayName ?: languageId} · Урок ${lesson.order}",
                step = step,
                total = if (run.phase == "REINFORCEMENT") 1 else steps,
                onBack = onBack
            )
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Загрузка урока…", color = CodonautColors.MochaGray)
            }
            state.error != null -> CenterMessage(state.error!!, "Вернуться", onBack)
            lesson != null && run?.phase == "COMPLETE" && state.answerCorrect == null -> CenterMessage(
                "Урок завершён!",
                if (state.xpAwarded > 0) "+${state.xpAwarded} XP" else "Можно пройти урок ещё раз",
                onBack,
                result = true
            )
            lesson != null && run?.phase == "THEORY" -> {
                val block = lesson.theoryBlocks.getOrNull(run.theoryIndex)
                if (block != null) {
                    Column(
                        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(block.title, fontFamily = Rubik, fontWeight = FontWeight.Bold,
                            fontSize = 24.sp, lineHeight = 30.sp, color = CodonautColors.EspressoBlack)
                        Text(block.text, fontFamily = Rubik, fontSize = 14.sp, lineHeight = 20.sp,
                            color = CodonautColors.MochaGray)
                        block.rememberNote?.let { TheoryRememberCard(it) }
                        block.codeExample?.let { LessonCodeBlock(it, languageId) }
                        if (block.codeExample != null) {
                            Text("В этом примере", fontFamily = Rubik, fontWeight = FontWeight.Bold,
                                fontSize = 15.sp, color = CodonautColors.EspressoBlack)
                            block.exampleExplanation?.let { explanation ->
                                Text(explanation, fontFamily = Rubik, fontSize = 13.sp, lineHeight = 19.sp,
                                    color = CodonautColors.MochaGray)
                            }
                        }
                        block.expectedOutput?.let {
                            Text("Результат:\n$it", Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)).background(CodonautColors.SoftCream)
                                .padding(14.dp), style = CodonautCodeStyle.copy(fontSize = 13.sp),
                                color = CodonautColors.DarkCocoa)
                        }
                        block.infoCard?.let { TheoryInfoCard(it.title, it.text, it.detail) }
                        Spacer(Modifier.height(4.dp))
                    }
                }
                LessonBottomButton(
                    if (run.theoryIndex + 1 == lesson.theoryBlocks.size) "К заданиям" else "Далее",
                    viewModel::nextTheory
                )
            }
            lesson != null && run != null && task != null -> {
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(task.question, fontFamily = Rubik, fontWeight = FontWeight.Bold,
                        fontSize = 24.sp, lineHeight = 30.sp, color = CodonautColors.EspressoBlack)
                    when (task) {
                        is SingleChoiceTaskContent -> TaskOptions(
                            options = task.options, selected = answer,
                            correctId = task.correctOptionId, checked = state.answerCorrect,
                            onSelect = { answer = it }
                        )
                        is OutputPredictionTaskContent -> {
                            LessonCodeBlock(task.codeSnippet, languageId)
                            TaskOptions(task.options, answer, task.correctOptionId, state.answerCorrect) { answer = it }
                        }
                        is CodeCompletionTaskContent -> {
                            LessonCodeBlock(task.codeTemplate, languageId)
                            OutlinedTextField(
                                value = answer,
                                onValueChange = { answer = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Недостающий фрагмент") },
                                placeholder = { Text(task.placeholder.orEmpty()) },
                                singleLine = true,
                                enabled = state.answerCorrect == null,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                        is OrderStepsTaskContent -> {
                            val stepsById = task.steps.associateBy { it.id }
                            order.forEachIndexed { index, id ->
                                OrderStepCard(
                                    text = stepsById[id]?.text.orEmpty(),
                                    canMoveUp = index > 0,
                                    canMoveDown = index < order.lastIndex,
                                    enabled = state.answerCorrect == null,
                                    moveUp = { order = order.toMutableList().also { it.add(index - 1, it.removeAt(index)) } },
                                    moveDown = { order = order.toMutableList().also { it.add(index + 1, it.removeAt(index)) } }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                if (state.answerCorrect == null) {
                    LessonBottomButton(
                        if (state.isSubmitting) "Проверяем…" else "Проверить",
                        onClick = {
                            val value = if (task is OrderStepsTaskContent) order.joinToString(",") else answer
                            viewModel.submitAnswer(value)
                        },
                        enabled = !state.isSubmitting &&
                            (task !is OrderStepsTaskContent && answer.isNotBlank() || task is OrderStepsTaskContent)
                    )
                } else {
                    TaskFeedbackBottomSheet(
                        correct = state.answerCorrect!!,
                        explanation = state.explanation.orEmpty(),
                        text = when {
                            run.reinforcementPending -> "Пройти закрепление"
                            !state.answerCorrect!! -> "Попробовать ещё"
                            else -> "Продолжить"
                        },
                        onClick = viewModel::continueAfterAnswer
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonTopBar(title: String, step: Int, total: Int, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CodonautBackButton(onClick = onBack)
            Text(title, Modifier.weight(1f), fontFamily = Rubik, fontWeight = FontWeight.Bold,
                fontSize = 14.sp, color = CodonautColors.EspressoBlack, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(10.dp))
            Surface(shape = CircleShape, color = CodonautColors.SoftCream) {
                Text("$step / $total", Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
                    fontFamily = Rubik, fontWeight = FontWeight.Medium, fontSize = 10.sp,
                    color = CodonautColors.DarkCocoa)
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(CodonautColors.WarmStone)) {
            Box(Modifier.fillMaxWidth((step.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f))
                .height(12.dp).clip(CircleShape).background(CodonautColors.DeepTeal))
        }
    }
}

@Composable
private fun LessonCodeBlock(code: String, languageId: String) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CodonautColors.DarkCocoa)) {
        Text(code, Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .padding(start = 14.dp, top = 26.dp, end = 14.dp, bottom = 14.dp),
            style = CodonautCodeStyle.copy(fontSize = 13.sp, lineHeight = 19.sp),
            color = CodonautColors.SoftCream, softWrap = false)
        Text(ProgrammingLanguage.fromId(languageId)?.displayName ?: languageId,
            Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 12.dp),
            fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium, fontSize = 10.sp,
            color = CodonautColors.WarmStone)
    }
}

@Composable
private fun TheoryRememberCard(text: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(CodonautColors.SoftCream)
            .border(1.dp, CodonautColors.Outline, RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(5.dp).height(58.dp).background(CodonautColors.Amber))
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Запомни", fontFamily = Rubik, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                color = CodonautColors.DeepTeal)
            Text(text, fontFamily = Rubik, fontSize = 12.sp, lineHeight = 16.sp,
                color = CodonautColors.DarkCocoa)
        }
    }
}

@Composable
private fun TheoryInfoCard(title: String, text: String, detail: String?) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CodonautColors.SoftCream)
            .border(1.dp, CodonautColors.Outline, RoundedCornerShape(16.dp)).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(painterResource(R.drawable.info_ic), contentDescription = null,
            modifier = Modifier.size(28.dp))
        Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontFamily = Rubik, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = CodonautColors.EspressoBlack)
            Text(text, fontFamily = Rubik, fontSize = 11.sp, lineHeight = 15.sp,
                color = CodonautColors.MochaGray)
            detail?.let { Text(it, fontFamily = Rubik, fontWeight = FontWeight.Medium,
                fontSize = 12.sp, lineHeight = 16.sp, color = CodonautColors.DarkCocoa) }
        }
    }
}

@Composable
private fun TaskOptions(
    options: List<AnswerOptionContent>,
    selected: String,
    correctId: String,
    checked: Boolean?,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option ->
            val isSelected = selected == option.id
            val isCorrectOption = checked != null && option.id == correctId
            val color = when {
                isCorrectOption -> CodonautColors.Success
                checked == false && isSelected -> CodonautColors.Error
                isSelected -> CodonautColors.DeepTeal
                else -> CodonautColors.Outline
            }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                    .background(CodonautColors.SoftCream)
                    .border(1.dp, color, RoundedCornerShape(13.dp))
                    .clickable(enabled = checked == null) { onSelect(option.id) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(18.dp).border(1.dp, color, CircleShape), contentAlignment = Alignment.Center) {
                    if (isSelected || isCorrectOption) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                    }
                }
                Text(option.text, Modifier.weight(1f).padding(start = 12.dp),
                    style = if ('\n' in option.text) CodonautCodeStyle.copy(fontSize = 12.sp, lineHeight = 17.sp)
                        else MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = CodonautColors.EspressoBlack)
            }
        }
    }
}

@Composable
private fun OrderStepCard(
    text: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    enabled: Boolean,
    moveUp: () -> Unit,
    moveDown: () -> Unit
) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(CodonautColors.SoftCream)
        .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(text, Modifier.weight(1f), style = CodonautCodeStyle.copy(fontSize = 12.sp, lineHeight = 18.sp),
            color = CodonautColors.EspressoBlack)
        Text("↑", Modifier.clickable(enabled && canMoveUp, onClick = moveUp).padding(10.dp),
            color = if (canMoveUp && enabled) CodonautColors.DeepTeal else CodonautColors.WarmStone)
        Text("↓", Modifier.clickable(enabled && canMoveDown, onClick = moveDown).padding(10.dp),
            color = if (canMoveDown && enabled) CodonautColors.DeepTeal else CodonautColors.WarmStone)
    }
}

@Composable
private fun TaskFeedbackBottomSheet(
    correct: Boolean,
    explanation: String,
    text: String,
    onClick: () -> Unit
) {
    val accent = if (correct) CodonautColors.Success else CodonautColors.Error
    val sheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = sheetShape,
        color = CodonautColors.SoftCream,
        shadowElevation = 8.dp
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painterResource(if (correct) R.drawable.correct_answer_ic else R.drawable.wrong_answer_ic),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Column(Modifier.padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (correct) "Верно!" else "Пока неверно", fontFamily = Rubik,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = accent)
                    Text(explanation, fontFamily = Rubik, fontSize = 14.sp, lineHeight = 19.sp,
                        color = CodonautColors.EspressoBlack)
                }
            }
            LessonBottomButton(
                text = text,
                onClick = onClick,
                containerColor = if (correct) CodonautColors.DeepTeal else CodonautColors.Error
            )
        }
    }
}

@Composable
private fun LessonBottomButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color = CodonautColors.DeepTeal
) {
    Button(onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)) {
        Text(text, fontFamily = Rubik, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun CenterMessage(title: String, subtitle: String, onBack: () -> Unit, result: Boolean = false) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            color = if (result) CodonautColors.Success else CodonautColors.EspressoBlack)
        Spacer(Modifier.height(10.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = CodonautColors.MochaGray)
        Spacer(Modifier.height(28.dp))
        LessonBottomButton("Вернуться", onBack)
    }
}
