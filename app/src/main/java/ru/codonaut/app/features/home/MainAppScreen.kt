package ru.codonaut.app.features.home

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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import ru.codonaut.app.R
import ru.codonaut.app.core.ui.theme.CodonautColors
import ru.codonaut.app.core.ui.theme.CodonautTheme
import ru.codonaut.app.domain.model.ProgrammingLanguage
import ru.codonaut.app.features.debug.SessionDebugScreen

enum class MainSection { Learning, Progress, Profile, Settings }

@Composable
fun MainAppScreen(
    onOpenLanguageSelection: () -> Unit,
    onOpenCourseMap: (String) -> Unit,
    onOpenLesson: (String, String) -> Unit,
    selectedSection: MainSection,
    onSectionSelected: (MainSection) -> Unit,
    onSignedOut: () -> Unit,
    onRegisterGuest: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val homeState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CodonautColors.WarmMist)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            when (selectedSection) {
                MainSection.Learning -> HomeScreen(
                    state = homeState,
                    onAllLanguages = onOpenLanguageSelection,
                    onContinueLearning = {
                        homeState.currentLesson?.let { onOpenLesson(it.language.id, it.lessonId) }
                    },
                    onOpenCourseMap = onOpenCourseMap
                )
                MainSection.Progress -> TemporarySectionScreen(
                    title = stringResource(R.string.main_nav_progress),
                    message = stringResource(R.string.home_section_coming_soon)
                )
                MainSection.Profile -> SessionDebugScreen(
                    onSignedOut = onSignedOut,
                    onRegisterGuest = onRegisterGuest
                )
                MainSection.Settings -> TemporarySectionScreen(
                    title = stringResource(R.string.main_nav_settings),
                    message = stringResource(R.string.home_section_coming_soon)
                )
            }
        }

        MainBottomNavigation(
            selectedSection = selectedSection,
            onSectionSelected = onSectionSelected,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onAllLanguages: () -> Unit,
    onContinueLearning: () -> Unit,
    onOpenCourseMap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
                fontWeight = FontWeight.Bold,
                color = CodonautColors.EspressoBlack
            )
            Text(
                text = stringResource(R.string.home_greeting_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = CodonautColors.MochaGray
            )
        }

        ProgressCard(state = state)

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle(
                    text = stringResource(R.string.home_continue_learning),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.home_all_languages),
                    style = MaterialTheme.typography.labelSmall,
                    color = CodonautColors.DeepTeal,
                    modifier = Modifier
                        .border(1.dp, CodonautColors.DeepTeal, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAllLanguages, role = Role.Button)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            CurrentLessonCard(state.currentLesson, onContinueLearning)
        }

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            SectionTitle(text = stringResource(R.string.home_my_languages))
            if (state.isLoading) {
                Text(
                    text = stringResource(R.string.home_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = CodonautColors.MochaGray
                )
            } else if (state.selectedLanguages.isEmpty()) {
                EmptyLanguagesCard(onAllLanguages)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.selectedLanguages.forEach { language ->
                        LanguageProgressCard(language = language) { onOpenCourseMap(language.id) }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            SectionTitle(text = stringResource(R.string.home_recommendation))
            RecommendationCard(state.recommendation)
        }
    }
}

@Composable
private fun ProgressCard(state: HomeUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(19.dp))
            .background(CodonautColors.DarkCocoa)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.home_your_progress),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = CodonautColors.WarmStone,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.home_xp_value, state.totalXp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = CodonautColors.EspressoBlack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CodonautColors.Amber)
                    .padding(horizontal = 15.dp, vertical = 5.dp)
            )
        }
        Text(
            text = state.levelTitle ?: stringResource(
                if (state.totalXp == 0L) R.string.home_initial_level
                else R.string.home_level_unavailable
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CodonautColors.SoftCream
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(CodonautColors.MochaGray)
        ) {
            state.levelProgress?.coerceIn(0f, 1f)?.let { progress ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(CodonautColors.Amber)
                )
            }
        }
        Text(
            text = state.levelProgressLabel
                ?: stringResource(
                    if (state.totalXp == 0L) R.string.home_progress_waiting
                    else R.string.home_progress_unknown_threshold
                ),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = CodonautColors.WarmStone
        )
    }
}

@Composable
private fun CurrentLessonCard(
    lesson: CurrentLessonUiModel?,
    onContinue: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(CodonautColors.SoftCream)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (lesson != null) {
            Image(
                painter = painterResource(languageIcon(lesson.language)),
                contentDescription = stringResource(languageName(lesson.language)),
                modifier = Modifier.height(48.dp).width(48.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        R.string.home_lesson_title,
                        stringResource(languageName(lesson.language)),
                        lesson.lessonNumber
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = CodonautColors.EspressoBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = lesson.topic,
                    style = MaterialTheme.typography.labelSmall,
                    color = CodonautColors.MochaGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Image(
                painter = painterResource(R.drawable.forward_allow_ic),
                contentDescription = stringResource(R.string.home_continue_action),
                modifier = Modifier
                    .height(52.dp)
                    .width(52.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(CodonautColors.DeepTeal)
                    .clickable(onClick = onContinue, role = Role.Button)
                    .padding(13.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CodonautColors.WarmMist),
                contentAlignment = Alignment.Center
            ) {
                Text("···", color = CodonautColors.DeepTeal, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_no_current_lesson),
                    style = MaterialTheme.typography.labelLarge,
                    color = CodonautColors.EspressoBlack
                )
                Text(
                    text = stringResource(R.string.home_lessons_coming_soon),
                    style = MaterialTheme.typography.labelSmall,
                    color = CodonautColors.MochaGray
                )
            }
        }
    }
}

@Composable
private fun LanguageProgressCard(language: ProgrammingLanguage, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(CodonautColors.SoftCream)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(languageIcon(language)),
                contentDescription = stringResource(languageName(language)),
                modifier = Modifier.height(34.dp).width(34.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = stringResource(languageName(language)),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CodonautColors.EspressoBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = stringResource(R.string.home_no_lesson_progress),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = CodonautColors.MochaGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape)
                .background(CodonautColors.WarmMist)
        )
    }
}

@Composable
private fun EmptyLanguagesCard(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.home_choose_languages),
        style = MaterialTheme.typography.bodySmall,
        color = CodonautColors.DeepTeal,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CodonautColors.SoftCream)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(16.dp)
    )
}

@Composable
private fun RecommendationCard(recommendation: RecommendationUiModel?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CodonautColors.Outline, RoundedCornerShape(17.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (recommendation != null) {
                Text(
                    text = stringResource(R.string.home_next_label),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = FontWeight.Bold,
                    color = CodonautColors.SoftCream,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CodonautColors.DeepTeal)
                        .padding(horizontal = 13.dp, vertical = 5.dp)
                )
                Text(
                    text = recommendation.topic,
                    style = MaterialTheme.typography.labelLarge,
                    color = CodonautColors.EspressoBlack
                )
                Text(
                    text = recommendation.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = CodonautColors.MochaGray
                )
            } else {
                Text(
                    text = stringResource(R.string.home_no_recommendation),
                    style = MaterialTheme.typography.labelLarge,
                    color = CodonautColors.EspressoBlack
                )
                Text(
                    text = stringResource(R.string.home_recommendation_coming_soon),
                    style = MaterialTheme.typography.labelSmall,
                    color = CodonautColors.MochaGray
                )
            }
        }
        if (recommendation != null) {
            Image(
                painter = painterResource(R.drawable.forward),
                contentDescription = stringResource(R.string.home_open_recommendation),
                colorFilter = ColorFilter.tint(CodonautColors.EspressoBlack),
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
                    .clip(CircleShape)
                    .background(CodonautColors.Amber)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
        fontWeight = FontWeight.Bold,
        color = CodonautColors.EspressoBlack,
        modifier = modifier
    )
}

@Composable
private fun TemporarySectionScreen(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = CodonautColors.EspressoBlack)
        Spacer(Modifier.height(10.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = CodonautColors.MochaGray)
    }
}

@Composable
internal fun MainBottomNavigation(
    selectedSection: MainSection,
    onSectionSelected: (MainSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CodonautColors.SoftCream)
            .border(1.dp, CodonautColors.Outline, RoundedCornerShape(18.dp))
            .padding(horizontal = 3.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainSection.entries.forEach { section ->
            val selected = selectedSection == section
            val icon = when (section) {
                MainSection.Learning -> R.drawable.book_ic
                MainSection.Progress -> R.drawable.progress_ic
                MainSection.Profile -> R.drawable.profile_ic
                MainSection.Settings -> R.drawable.settings_ic
            }
            val title = when (section) {
                MainSection.Learning -> R.string.main_nav_learning
                MainSection.Progress -> R.string.main_nav_progress
                MainSection.Profile -> R.string.main_nav_profile
                MainSection.Settings -> R.string.main_nav_settings
            }
            val titleText = stringResource(title)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        onClick = { onSectionSelected(section) },
                        role = Role.Tab
                    )
                    .semantics {
                        contentDescription = titleText
                        this.selected = selected
                    }
                    .padding(vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        if (selected) CodonautColors.DeepTeal else CodonautColors.MochaGray
                    ),
                    modifier = Modifier.height(25.dp).width(25.dp)
                )
                Text(
                    text = stringResource(title),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) CodonautColors.DeepTeal else CodonautColors.MochaGray,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .width(if (selected) 30.dp else 0.dp)
                        .height(if (selected) 3.dp else 0.dp)
                        .clip(CircleShape)
                        .background(if (selected) CodonautColors.DeepTeal else Color.Transparent)
                )
            }
        }
    }
}

private fun languageIcon(language: ProgrammingLanguage): Int = when (language) {
    ProgrammingLanguage.PYTHON -> R.drawable.python_ic
    ProgrammingLanguage.JAVASCRIPT -> R.drawable.javascript_ic
    ProgrammingLanguage.JAVA -> R.drawable.java_ic
}

private fun languageName(language: ProgrammingLanguage): Int = when (language) {
    ProgrammingLanguage.PYTHON -> R.string.language_python
    ProgrammingLanguage.JAVASCRIPT -> R.string.language_javascript
    ProgrammingLanguage.JAVA -> R.string.language_java
}

@Preview(showBackground = true, backgroundColor = 0xFFE7E1DA, widthDp = 360)
@Composable
private fun HomeScreenDesignPreview() {
    CodonautTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                selectedLanguages = listOf(ProgrammingLanguage.PYTHON, ProgrammingLanguage.JAVASCRIPT),
                totalXp = 240,
                levelTitle = "Уровень 3",
                levelProgress = 0.6f,
                levelProgressLabel = "240 / 400 XP до следующего уровня",
                currentLesson = CurrentLessonUiModel(ProgrammingLanguage.PYTHON, 4, "Условия и ветвления", "python.lesson_04"),
                recommendation = RecommendationUiModel("Логические условия", "Подойдёт после текущего урока")
            ),
            onAllLanguages = {},
            onContinueLearning = {},
            onOpenCourseMap = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7E1DA, widthDp = 360)
@Composable
private fun NewUserHomeScreenPreview() {
    CodonautTheme {
        HomeScreen(state = HomeUiState(isLoading = false), onAllLanguages = {}, onContinueLearning = {}, onOpenCourseMap = {})
    }
}
