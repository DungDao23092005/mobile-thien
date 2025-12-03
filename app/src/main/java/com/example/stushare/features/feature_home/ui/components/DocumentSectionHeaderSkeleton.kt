package com.example.stushare.features.feature_home.ui.components

// File: features/feature_home/ui/components/DocumentSectionHeaderSkeleton.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stushare.ui.theme.createShimmerBrush

@Composable
fun DocumentSectionHeaderSkeleton(modifier: Modifier = Modifier) {
    val brush = createShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Tiêu đề section
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(22.dp)
                .background(brush, RoundedCornerShape(4.dp))
        )
        // Nút "Xem tất cả"
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(18.dp)
                .background(brush, RoundedCornerShape(4.dp))
        )
    }
}