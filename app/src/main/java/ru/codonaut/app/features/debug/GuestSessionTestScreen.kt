package ru.codonaut.app.features.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.codonaut.app.core.ui.components.BaseButton

@Composable
fun GuestSessionTestScreen() {

    val viewModel: GuestSessionViewModel = viewModel(
        factory = GuestSessionViewModel.Factory
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Проверка гостевого режима",
            style = MaterialTheme.typography.headlineMedium
        )

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {

            val profile = uiState.profile

            if (profile == null) {
                Text(
                    text = "Активный профиль пока не найден",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Профиль успешно загружен",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "ID: ${profile.profileId}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Никнейм: ${profile.nickname ?: "не задан"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (profile.authUid == null) {
                        "Тип: гостевой профиль"
                    } else {
                        "Тип: привязанный аккаунт"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            BaseButton(
                text = "Продолжить как гость",
                onClick = viewModel::continueAsGuest
            )
        }
    }
}
