package ru.codonaut.app.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import ru.codonaut.app.core.ui.theme.CodonautColors
import androidx.compose.foundation.Image
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.size
import ru.codonaut.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.tooling.preview.Preview
import ru.codonaut.app.core.ui.theme.CodonautTheme
import androidx.compose.ui.graphics.Color

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit, // поле не хранит email/пароль
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    containerColor: Color = CodonautColors.SoftCream,
) {
    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Column(modifier = modifier) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            textStyle = MaterialTheme.typography.bodyMedium,

            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium
                )
            },

            isError = error != null,

            visualTransformation =
                if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },

            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) {
                    KeyboardType.Password
                } else {
                    keyboardType
                },
                imeAction = imeAction
            ),

            trailingIcon = if (isPassword) {
                {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        },
                        enabled = enabled
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (passwordVisible) {
                                    R.drawable.eye
                                } else {
                                    R.drawable.eye_off
                                }
                            ),
                            contentDescription = stringResource(
                                if (passwordVisible) {
                                    R.string.hide_password
                                } else {
                                    R.string.show_password
                                }
                            ),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            } else {
                null
            },

            supportingText = if (error != null) {
                {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                null
            },

            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                disabledTextColor = containerColor,
                errorContainerColor = containerColor,

                focusedBorderColor = CodonautColors.DeepTeal,
                unfocusedBorderColor = CodonautColors.Outline,
                errorBorderColor = CodonautColors.Error,

                focusedTextColor = CodonautColors.EspressoBlack,
                unfocusedTextColor = CodonautColors.EspressoBlack,

                cursorColor = CodonautColors.DeepTeal
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun CodonautTextFieldPreview() {
    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by rememberSaveable {
        mutableStateOf("")
    }

    CodonautTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = "Электронная почта",
                placeholder = "example@mail.com",
                keyboardType = KeyboardType.Email
            )

           TextField(
                value = password,
                onValueChange = { password = it },
                label = "Пароль",
                placeholder = "Введите пароль",
                isPassword = true
            )

            TextField(
                value = "",
                onValueChange = {},
                label = "Подтверждение пароля",
                error = "Пароли не совпадают",
                isPassword = true
            )
        }
    }
}

