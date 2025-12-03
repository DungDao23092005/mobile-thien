package com.example.stushare.features.feature_search.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stushare.ui.theme.PrimaryGreen // Import màu xanh

@Composable
fun SearchTagChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(16.dp),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = PrimaryGreen.copy(alpha = 0.1f), // Màu nền xanh nhạt
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = null, // Không có viền
        modifier = modifier
    )
}