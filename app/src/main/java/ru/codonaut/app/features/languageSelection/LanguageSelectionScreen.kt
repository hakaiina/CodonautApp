package ru.codonaut.app.features.languageSelection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.codonaut.app.R
import ru.codonaut.app.core.ui.components.BaseButton
import ru.codonaut.app.core.ui.theme.CodonautColors
import ru.codonaut.app.core.ui.theme.CodonautTheme
import ru.codonaut.app.domain.model.ProgrammingLanguage

private data class LanguageOption(
    val language: ProgrammingLanguage,
    val iconRes: Int,
    val descriptionRes: Int
)

private val languageOptions = listOf(
    LanguageOption(ProgrammingLanguage.PYTHON, R.drawable.python_ic, R.string.language_python_description),
    LanguageOption(ProgrammingLanguage.JAVASCRIPT, R.drawable.javascript_ic, R.string.language_javascript_description),
    LanguageOption(ProgrammingLanguage.JAVA, R.drawable.java_ic, R.string.language_java_description)
)

@Composable
fun LanguageSelectionScreen(
    onBackClick: () -> Unit,
    onSelectionSaved: () -> Unit,
    viewModel: LanguageSelectionViewModel = viewModel(
        factory = LanguageSelectionViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LanguageSelectionContent(
        state = state,
        onBackClick = onBackClick,
        onToggleLanguage = viewModel::toggleLanguage,
        onContinue = { viewModel.saveSelection(onSelectionSaved) }
    )
}

@Composable
private fun LanguageSelectionContent(
    state: LanguageSelectionUiState,
    onBackClick: () -> Unit,
    onToggleLanguage: (ProgrammingLanguage) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CodonautColors.WarmMist)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 25.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.back),
            contentDescription = stringResource(R.string.language_selection_back),
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onBackClick)
                .padding(4.dp)
        )

        Spacer(Modifier.height(30.dp))
        Text(
            text = stringResource(R.string.language_selection_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = CodonautColors.EspressoBlack,
            modifier = Modifier.padding(start = 14.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.language_selection_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = CodonautColors.MochaGray,
            modifier = Modifier.padding(start = 18.dp)
        )
        Spacer(Modifier.height(35.dp))

        languageOptions.forEach { option ->
            val selected = option.language.id in state.selectedLanguageIds
            LanguageCard(
                option = option,
                selected = selected,
                enabled = !state.isLoading && !state.isSaving,
                onClick = { onToggleLanguage(option.language) }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CodonautColors.DeepTeal)
            }
        }

        state.errorMessageRes?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                color = CodonautColors.Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.weight(1f))
        BaseButton(
            text = stringResource(
                if (state.isSaving) R.string.language_selection_saving
                else R.string.language_selection_continue
            ),
            onClick = onContinue,
            enabled = state.canContinue,
            modifier = Modifier.padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun LanguageCard(
    option: LanguageOption,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(
                width = 1.dp,
                color = if (selected) CodonautColors.DeepTeal else CodonautColors.Outline,
                shape = RoundedCornerShape(18.dp)
            )
            .background(CodonautColors.SoftCream, RoundedCornerShape(18.dp))
            .semantics { this.selected = selected }
            .clickable(enabled = enabled, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(option.iconRes),
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = stringResource(
                    when (option.language) {
                        ProgrammingLanguage.PYTHON -> R.string.language_python
                        ProgrammingLanguage.JAVASCRIPT -> R.string.language_javascript
                        ProgrammingLanguage.JAVA -> R.string.language_java
                    }
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CodonautColors.EspressoBlack
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = stringResource(option.descriptionRes),
                style = MaterialTheme.typography.labelMedium,
                color = CodonautColors.MochaGray
            )
        }
        Spacer(Modifier.width(12.dp))
        SelectionIndicator(selected)
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean) {
    if (selected) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(CodonautColors.DeepTeal, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = CodonautColors.SoftCream, style = MaterialTheme.typography.labelSmall)
        }
    } else {
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(1.5.dp, CodonautColors.Outline, CircleShape)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7E1DA)
@Composable
private fun LanguageSelectionPreview() {
    CodonautTheme {
        LanguageSelectionContent(
            state = LanguageSelectionUiState(
                selectedLanguageIds = setOf("python", "javascript"),
                isLoading = false
            ),
            onBackClick = {},
            onToggleLanguage = {},
            onContinue = {}
        )
    }
}
