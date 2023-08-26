package com.dk.piley.ui.common

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.dk.piley.ui.theme.PileyTheme

/**
 * Editable title text element
 *
 * @param value text value
 * @param enabled whether editing is enabled
 * @param onValueChange on text value change
 */
@Composable
fun EditableTitleText(
    value: String,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit = {}
) {
    val sizeBasedTextStyle = when (value.length) {
        in 0..20 -> MaterialTheme.typography.headlineSmall
        in 21..40 -> MaterialTheme.typography.titleMedium
        in 41..60 -> MaterialTheme.typography.bodyMedium
        else -> MaterialTheme.typography.bodySmall
    }
    TextField(
        value = value,
        enabled = enabled,
        onValueChange = onValueChange,
        modifier = Modifier.wrapContentSize(Alignment.Center),
        label = null,
        textStyle = sizeBasedTextStyle
            .copy(textDecoration = TextDecoration.None)
            .copy(textAlign = TextAlign.Center),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            disabledTrailingIconColor = Color.Transparent,
        )
    )
}

@Preview
@Composable
fun PileDetailSettingsPreview() {
    PileyTheme(useDarkTheme = true) {
        EditableTitleText(value = "Some title here")
    }
}

@Preview
@Composable
fun PileDetailSettingsPreviewDisabled() {
    PileyTheme(useDarkTheme = true) {
        EditableTitleText(value = "Some title here", enabled = false)
    }
}