package ru.codonaut.app.features.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.codonaut.app.core.ui.components.TextField
import ru.codonaut.app.core.ui.theme.CodonautColors
import ru.codonaut.app.core.ui.theme.CodonautTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    onRegistrationClick: () -> Unit,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            viewModel.onCompletionHandled()
            onAuthenticated()
        }
    }

    LoginContent(
        state = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onSignIn = viewModel::signIn,
        onContinueAsGuest = viewModel::continueAsGuest,
        onRegistrationClick = onRegistrationClick
    )
}


@Composable
private fun LoginContent(
    state: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onRegistrationClick: () -> Unit
) {
    val fieldColor = Color(0xFFC4BFBA)
    val guestButtonColor = Color(0xFF7D9C98)
    val registrationColor = Color(0xFF6658F5)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CodonautColors.WarmMist)
    ) {
        val topSpacing = if (maxHeight < 720.dp) {
            56.dp
        } else {
            130.dp
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = topSpacing,
                    bottom = 120.dp
                )
        ) {

            Text(
                text = "Вход в аккаунт",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CodonautColors.EspressoBlack
            )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = state.email,
                onValueChange = onEmailChanged,
                label = "Email",
                placeholder = "Электронная почта",
                containerColor = fieldColor,
                error = state.emailError,
                enabled = !state.isLoading,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(18.dp))

            TextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = "Пароль",
                placeholder = "Пароль",
                containerColor = fieldColor,
                error = state.passwordError,
                enabled = !state.isLoading,
                isPassword = true,
                imeAction = ImeAction.Done
            )

            if (state.generalError != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = state.generalError,
                    color = CodonautColors.Error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onSignIn,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CodonautColors.DeepTeal,
                    contentColor = Color.White
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Войти",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueAsGuest,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = guestButtonColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Продолжить как гость",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    horizontal = 8.dp,
                    vertical = 52.dp
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Нет аккаунта?",
                fontSize = 14.sp,
                color = CodonautColors.EspressoBlack
            )

            Text(
                text = " Зарегистрироваться",
                modifier = Modifier.clickable(
                    enabled = !state.isLoading,
                    onClick = onRegistrationClick
                ),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = registrationColor
            )
        }
    }
}


