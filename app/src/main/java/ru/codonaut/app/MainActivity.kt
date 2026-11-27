package ru.codonaut.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ru.codonaut.app.core.ui.theme.CodonautTheme
import ru.codonaut.app.features.auth.login.LoginScreen
import ru.codonaut.app.features.auth.registration.RegistrationScreen
import ru.codonaut.app.features.onboarding.OnboardingScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import ru.codonaut.app.features.startup.StartupUiState
import ru.codonaut.app.features.startup.StartupViewModel
import ru.codonaut.app.features.languageSelection.LanguageSelectionScreen
import ru.codonaut.app.features.home.MainAppScreen
import ru.codonaut.app.features.learning.CourseMapScreen
import ru.codonaut.app.features.learning.LessonScreen
import ru.codonaut.app.features.home.MainSection
import androidx.compose.runtime.saveable.rememberSaveable

private data object OnboardingRoute
private data object LoginRoute
private data object RegistrationRoute
private data object StartupRoute
private data object LanguageSelectionRoute
private data object MainRoute
private data class CourseMapRoute(val languageId: String)
private data class LessonRoute(val languageId: String, val lessonId: String)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CodonautTheme {
                CodonautNavigation()
            }
        }
    }
}

@Composable
private fun CodonautNavigation() {

    var mainSection by rememberSaveable { mutableStateOf(MainSection.Learning) }

    var backStack by remember {
        mutableStateOf<List<Any>>(
            listOf(StartupRoute)
        )
    }

    // заменяем весь стек за одну операцию
    fun navigateAsRoot(route: Any) {
        backStack = listOf(route)
    }

    // открываем следующий экран
    fun navigateTo(route: Any) {
        if (backStack.lastOrNull() != route) {
            backStack = backStack + route
        }
    }

    // возвращаемся на предыдущий экран
    fun navigateBack() {
        if (backStack.size > 1) {
            backStack = backStack.dropLast(1)
        }
    }

    val startupViewModel: StartupViewModel = viewModel(
        factory = StartupViewModel.Factory
    )

    val startupState by startupViewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(startupState) {
        val currentRoute = backStack.singleOrNull()

        when (startupState) {
            StartupUiState.Onboarding -> {
                if (currentRoute == StartupRoute) {
                    navigateAsRoot(OnboardingRoute)
                }
            }

            StartupUiState.Login -> {
                if (
                    currentRoute == StartupRoute ||
                    currentRoute == OnboardingRoute
                ) {
                    navigateAsRoot(LoginRoute)
                }
            }

            StartupUiState.Session -> {
                if (currentRoute == StartupRoute) {
                    navigateAsRoot(MainRoute)
                }
            }

            StartupUiState.LanguageSelection -> {
                if (currentRoute == StartupRoute) {
                    navigateAsRoot(LanguageSelectionRoute)
                }
            }

            StartupUiState.Error -> {
                if (currentRoute == OnboardingRoute) {
                    navigateAsRoot(StartupRoute)
                }
            }

            StartupUiState.Loading -> Unit
        }
    }

    NavDisplay(
        backStack = backStack,

        onBack = {
            navigateBack()
        },

        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),

        entryProvider = { key ->
            when (key) {

                OnboardingRoute -> NavEntry(key) {
                    OnboardingScreen(
                        onFinished = startupViewModel::completeOnboarding
                    )
                }

                LoginRoute -> NavEntry(key) {
                    LoginScreen(
                        onAuthenticated = {
                            navigateAsRoot(StartupRoute)
                            startupViewModel.refreshSessionDestination()
                        },
                        onRegistrationClick = {
                            navigateTo(RegistrationRoute)
                        }
                    )
                }

                RegistrationRoute -> NavEntry(key) {
                    RegistrationScreen(
                        onBackClick = {
                            navigateBack()
                        },
                        onRegistered = {
                            navigateAsRoot(StartupRoute)
                            startupViewModel.refreshSessionDestination()
                        }
                    )
                }

                StartupRoute -> NavEntry(key) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (startupState == StartupUiState.Error) {
                            Button(onClick = startupViewModel::restore) {
                                Text("Повторить загрузку")
                            }
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                }

                LanguageSelectionRoute -> NavEntry(key) {
                    LanguageSelectionScreen(
                        onBackClick = {
                            if (backStack.size > 1) navigateBack()
                            else navigateAsRoot(LoginRoute)
                        },
                        onSelectionSaved = {
                            navigateAsRoot(MainRoute)
                        }
                    )
                }

                MainRoute -> NavEntry(key) {
                    MainAppScreen(
                        onOpenLanguageSelection = {
                            navigateTo(LanguageSelectionRoute)
                        },
                        onSignedOut = {
                            navigateAsRoot(LoginRoute)
                        },
                        onRegisterGuest = {
                            navigateTo(RegistrationRoute)
                        },
                        selectedSection = mainSection,
                        onSectionSelected = { mainSection = it },
                        onOpenCourseMap = { languageId ->
                            navigateTo(CourseMapRoute(languageId))
                        },
                        onOpenLesson = { languageId, lessonId ->
                            navigateTo(LessonRoute(languageId, lessonId))
                        }
                    )
                }

                is CourseMapRoute -> NavEntry(key) {
                    CourseMapScreen(
                        languageId = key.languageId,
                        onBack = ::navigateBack,
                        onStartLesson = { languageId, lessonId -> navigateTo(LessonRoute(languageId, lessonId)) },
                        onSectionSelected = { section ->
                            mainSection = section
                            val mainIndex = backStack.indexOf(MainRoute)
                            backStack = if (mainIndex >= 0) backStack.take(mainIndex + 1) else listOf(MainRoute)
                        }
                    )
                }

                is LessonRoute -> NavEntry(key) {
                    LessonScreen(
                        languageId = key.languageId,
                        lessonId = key.lessonId,
                        onBack = ::navigateBack
                    )
                }

                else -> error(
                    "Unknown navigation route: $key"
                )
            }
        }
    )
}
