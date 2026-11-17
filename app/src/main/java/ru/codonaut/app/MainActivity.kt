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
import ru.codonaut.app.features.debug.SessionDebugScreen
import ru.codonaut.app.features.onboarding.OnboardingScreen
import java.util.UUID
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

private data object OnboardingRoute
private data object LoginRoute
private data object RegistrationRoute
private data object StartupRoute

// у каждого открытия экрана сессии собственный ключ
private data class SessionDebugRoute(
    val instanceId: String = UUID.randomUUID().toString()
)

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
                    navigateAsRoot(SessionDebugRoute())
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
                            navigateAsRoot(SessionDebugRoute())
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
                            navigateAsRoot(SessionDebugRoute())
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

                is SessionDebugRoute -> NavEntry(key) {
                    SessionDebugScreen(
                        onSignedOut = {
                            navigateAsRoot(LoginRoute)
                        },
                        onRegisterGuest = {
                            navigateTo(RegistrationRoute)
                        }
                    )
                }

                else -> error(
                    "Unknown navigation route: $key"
                )
            }
        }
    )
}
