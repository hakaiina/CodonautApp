package ru.codonaut.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import ru.codonaut.app.core.ui.theme.CodonautTheme
import ru.codonaut.app.features.debug.GuestSessionTestScreen
import ru.codonaut.app.features.onboarding.OnboardingScreen

// объекты-маркеры, обозначающие направления навигации
private data object OnboardingRoute
private data object LoginRoute

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
    val backStack = remember {
        mutableStateListOf<Any>(OnboardingRoute)
    }

    // получаем список маршрутов и строим соответствующий интерфейс
    NavDisplay(
        // backStack хранит историю переходов пользователя по экранам приложения
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex) // если в стеке больше одного маршрута, удаляется последний
            }
        },
        entryProvider = { key ->
            when (key) {
                OnboardingRoute -> NavEntry(key) {
                    OnboardingScreen(
                        // после завершения онбординга
                        onFinished = {
                            backStack.clear() // очищаем стек
                            backStack.add(LoginRoute) // переходим к авторизации
                        }
                    )
                }

                LoginRoute -> NavEntry(key) {
                    GuestSessionTestScreen()
                }

                else -> error("Unknown navigation route: $key") // неизвестный маршрут
            }
        }
    )
}

