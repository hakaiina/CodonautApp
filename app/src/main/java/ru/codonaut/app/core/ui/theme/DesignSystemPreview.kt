package ru.codonaut.app.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.codonaut.app.core.ui.theme.CodonautTheme
import androidx.compose.foundation.layout.padding
import ru.codonaut.app.core.ui.components.BaseButton

@Preview(
    showBackground = true,
    widthDp = 390,
    heightDp = 760
)
@Composable
private fun CodonautDesignPreview() {
    CodonautTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(CodonautSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(
                CodonautSpacing.md
            )
        ) {
            Text(
                text = "Codonaut",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Открой вселенную программирования",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Изучай программирование шаг за шагом",
                style = MaterialTheme.typography.bodyMedium
            )

            Surface(
                color = CodonautColors.DarkCocoa,
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "Твой путь начинается здесь",
                    modifier = Modifier.padding(
                        CodonautSpacing.md
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CodonautColors.SoftCream
                )
            }

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text("Продолжить")
            }

            Text(
                text = "print(\"Hello, Codonaut!\")",
                style = CodonautCodeStyle
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CodonautButtonPreview() {
    CodonautTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            BaseButton(
                text = "Далее",
                onClick = {}
            )
        }
    }
}