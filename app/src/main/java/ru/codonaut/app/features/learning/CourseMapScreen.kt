package ru.codonaut.app.features.learning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.codonaut.app.R
import ru.codonaut.app.core.ui.components.CodonautBackButton
import ru.codonaut.app.core.ui.theme.CodonautColors
import ru.codonaut.app.core.ui.theme.Rubik
import ru.codonaut.app.domain.model.ProgrammingLanguage
import ru.codonaut.app.domain.model.content.LessonContent
import ru.codonaut.app.domain.usecase.LessonAccessRules
import ru.codonaut.app.features.home.MainBottomNavigation
import ru.codonaut.app.features.home.MainSection

private enum class PlanetState { DONE, CURRENT, LOCKED }
private data class MapPoint(val x: androidx.compose.ui.unit.Dp, val y: androidx.compose.ui.unit.Dp)

@Composable
fun CourseMapScreen(
    languageId: String,
    onBack: () -> Unit,
    onStartLesson: (String, String) -> Unit,
    onSectionSelected: (MainSection) -> Unit,
    viewModel: CourseMapViewModel = viewModel(
        key = "course-map-$languageId",
        factory = CourseMapViewModel.factory(languageId)
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val language = ProgrammingLanguage.fromId(languageId)
    val completed = state.completedLessonIds
    val firstAvailable = LessonAccessRules.firstAvailable(state.lessons, completed)
    val defaultSelection = firstAvailable ?: state.lessons.lastOrNull { it.id in completed }
    var selectedId by remember(languageId) { mutableStateOf<String?>(defaultSelection?.id) }
    LaunchedEffect(firstAvailable?.id, state.lessons, completed) {
        if (selectedId == null || state.lessons.none { it.id == selectedId }) {
            selectedId = (firstAvailable ?: state.lessons.lastOrNull { it.id in completed })?.id
        }
    }
    val resolvedSelectionId = selectedId ?: defaultSelection?.id
    val selected = state.lessons.firstOrNull { it.id == resolvedSelectionId }
    val selectedDone = selected?.id in completed

    Column(
        Modifier
            .fillMaxSize()
            .background(CodonautColors.WarmMist)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(48.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            CodonautBackButton(onClick = onBack)
            Text(
                language?.displayName ?: languageId, Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = CodonautColors.EspressoBlack
            )
        }

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CodonautColors.DarkCocoa)
                .padding(14.dp)
        ) {
            val points = listOf(
                MapPoint(maxWidth * .77f, maxHeight * .14f),
                MapPoint(maxWidth * .52f, maxHeight * .26f),
                MapPoint(maxWidth * .64f, maxHeight * .40f),
                MapPoint(maxWidth * .49f, maxHeight * .54f),
                MapPoint(maxWidth * .33f, maxHeight * .69f),
                MapPoint(maxWidth * .49f, maxHeight * .83f)
            )
            Text(
                "ПУТЬ ПО ГАЛАКТИКЕ",
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 3.dp, top = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = CodonautColors.WarmStone
            )
            Canvas(Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(points.first().x.toPx(), points.first().y.toPx())
                    points.zipWithNext().forEachIndexed { index, (a, b) ->
                        val previous = points.getOrElse(index - 1) { a }
                        val next = points.getOrElse(index + 2) { b }
                        val control1 = MapPoint(
                            a.x + (b.x - previous.x) / 6f,
                            a.y + (b.y - previous.y) / 6f
                        )
                        val control2 = MapPoint(
                            b.x - (next.x - a.x) / 6f,
                            b.y - (next.y - a.y) / 6f
                        )
                        cubicTo(
                            control1.x.toPx(),
                            control1.y.toPx(),
                            control2.x.toPx(),
                            control2.y.toPx(),
                            b.x.toPx(),
                            b.y.toPx()
                        )
                    }
                }
                drawPath(
                    path,
                    color = CodonautColors.WarmStone,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx())
                )
            }
            // Декоративная планета слева
            Image(
                painter = painterResource(R.drawable.planet_1_ic),
                contentDescription = null,
                modifier = Modifier
                    .offset(
                        x = maxWidth * .08f - 7.dp,
                        y = maxHeight * .36f - 7.dp
                    )
                    .size(56.dp)
            )

// Декоративная планета справа снизу
            Image(
                painter = painterResource(R.drawable.planet_2_ic),
                contentDescription = null,
                modifier = Modifier
                    .offset(
                        x = maxWidth * .69f - 7.dp,
                        y = maxHeight * .76f - 7.dp
                    )
                    .size(58.dp)
            )

// Декоративные звёзды
            listOf(
                Offset(.16f, .55f),
                Offset(.83f, .37f),
                Offset(.79f, .65f),
                Offset(.12f, .81f)
            ).forEach { position ->
                Image(
                    painter = painterResource(R.drawable.star_ic),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(
                            x = maxWidth * position.x - 3.dp,
                            y = maxHeight * position.y - 3.dp
                        )
                        .size(18.dp)
                )
            }
            if (state.isLoading) CircularProgressIndicator(
                Modifier.align(Alignment.Center),
                color = CodonautColors.Amber
            )
            else (0 until 6).forEach { index ->
                val lesson = state.lessons.getOrNull(index)
                val isDone = lesson != null && lesson.id in completed
                val isCurrent = lesson != null && LessonAccessRules.isAccessible(
                    lesson,
                    state.lessons,
                    completed
                ) && !isDone
                val status = when {
                    isDone -> PlanetState.DONE; isCurrent -> PlanetState.CURRENT; else -> PlanetState.LOCKED
                }
                val currentSize = (maxHeight / 5.5f)
                    .coerceAtMost(86.dp)
                    .coerceAtLeast(66.dp)

                val size = if (status == PlanetState.LOCKED)
                    currentSize - 6.dp
                else
                    currentSize
                val icon = when (status) {
                    PlanetState.DONE -> R.drawable.done_level_ic
                    PlanetState.CURRENT -> R.drawable.current_level_ic
                    PlanetState.LOCKED -> R.drawable.lock_level_ic
                }
                Box(
                    Modifier
                        .offset(points[index].x - size / 2, points[index].y - size / 2)
                        .size(size)
                        .then(if (lesson != null && status != PlanetState.LOCKED) Modifier.clickable {
                            selectedId = lesson.id
                        } else Modifier),
                    contentAlignment = Alignment.Center) {
                    Image(
                        painterResource(icon), contentDescription = when (status) {
                            PlanetState.DONE -> "Уровень ${lesson?.order}, завершён"
                            PlanetState.CURRENT -> "Уровень ${lesson?.order}, доступен"
                            PlanetState.LOCKED -> "Уровень ${lesson?.order ?: index + 1}, заблокирован"
                        }, modifier = Modifier.fillMaxSize()
                    )
                    if (status != PlanetState.LOCKED) Text(
                        "${lesson?.order}",
                        color = Color.White,
                        fontFamily = Rubik,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        style = MaterialTheme.typography.displaySmall.copy(
                            shadow = Shadow(
                                Color.Black.copy(.65f),
                                Offset(1.5f, 3f),
                                4f
                            )
                        )
                    )
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 0.dp, bottom = 0.dp)
                .shadow(5.dp, RoundedCornerShape(18.dp))
                .background(CodonautColors.SoftCream, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (state.lessons.isEmpty() && !state.isLoading) {
                Text(
                    "Уроки пока недоступны", style = MaterialTheme.typography.titleSmall,
                    color = CodonautColors.EspressoBlack, fontWeight = FontWeight.Bold
                )
                Text(
                    "Материалы курса появятся здесь позже",
                    style = MaterialTheme.typography.labelSmall,
                    color = CodonautColors.MochaGray
                )
            } else if (selected != null) {
                Text(
                    selected.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    fontFamily = Rubik,
                    fontWeight = FontWeight.Bold,
                    color = CodonautColors.EspressoBlack
                )
                Button(
                    onClick = { onStartLesson(languageId, selected.id) },
                    enabled = selectedDone || selected.id == firstAvailable?.id,
                    modifier = Modifier
                        .width(205.dp)
                        .height(41.dp),
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CodonautColors.DeepTeal)
                ) {
                    Text(
                        if (selectedDone) "Повторить урок" else "Начать уровень",
                        fontFamily = Rubik,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    "Выбери доступный уровень", style = MaterialTheme.typography.labelLarge,
                    color = CodonautColors.MochaGray
                )
            }
        }

        MainBottomNavigation(
            selectedSection = MainSection.Learning,
            onSectionSelected = onSectionSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 0.dp, top = 8.dp, bottom = 3.dp)
        )
    }
}
