package ru.codonaut.app.features.auth.registration

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.codonaut.app.R
import ru.codonaut.app.core.ui.components.TextField
import ru.codonaut.app.core.ui.theme.CodonautColors
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible

@Composable
fun RegistrationScreen(
    onBackClick: () -> Unit,
    onRegistered: () -> Unit,
    viewModel: RegistrationViewModel = viewModel(
        factory = RegistrationViewModel.Factory
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            viewModel.onCompletionHandled()
            onRegistered()
        }
    }

    RegistrationContent(
        state = uiState,
        onNicknameChanged = viewModel::onNicknameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onRegister = viewModel::register,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun RegistrationContent(
    state: RegistrationUiState,
    onNicknameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onBackClick: () -> Unit
) {
    val fieldColor = Color(0xFFC4BFBA)
    val loginLinkColor = Color(0xFF6658F5)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CodonautColors.WarmMist)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                enabled = !state.isLoading,
                modifier = Modifier.offset(x = (-10).dp, y = 8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Вернуться ко входу",
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Создайте свой аккаунт",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CodonautColors.EspressoBlack
            )

            Spacer(modifier = Modifier.height(28.dp))

            TextField(
                value = state.nickname,
                onValueChange = onNicknameChanged,
                label = "Имя пользователя",
                placeholder = "Как к тебе обращаться",
                containerColor = fieldColor,
                error = state.nicknameError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = "Пароль",
                placeholder = "Придумай надёжный пароль",
                containerColor = fieldColor,
                error = state.passwordError,
                enabled = !state.isLoading,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = "Повтори пароль",
                placeholder = "Подтверждение пароля",
                containerColor = fieldColor,
                error = state.confirmPasswordError,
                enabled = !state.isLoading,
                isPassword = true,
                imeAction = ImeAction.Done
            )

            if (state.generalError != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = state.generalError,
                    style = MaterialTheme.typography.bodySmall,
                    color = CodonautColors.Error
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onRegister,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
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
                        text = "Зарегистрироваться",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
        // показываем нижнюю ссылку только при закрытой клавиатуре
        if (!WindowInsets.isImeVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Уже есть аккаунт? ",
                    fontSize = 14.sp,
                    color = CodonautColors.EspressoBlack
                )

                Text(
                    text = "Войти",
                    modifier = Modifier.clickable(
                        enabled = !state.isLoading,
                        onClick = onBackClick
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = loginLinkColor
                )
            }
        }
    }
}