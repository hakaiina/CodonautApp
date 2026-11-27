package ru.codonaut.app.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.codonaut.app.core.ui.theme.CodonautColors

@Composable
fun CodonautBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String = "Назад"
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Text(
            text = "‹",
            fontSize = 32.sp,
            lineHeight = 32.sp,
            color = CodonautColors.EspressoBlack
        )
    }
}
