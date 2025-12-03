package com.example.stushare.features.feature_home.ui.home

// File: features/feature_home/ui/components/HomeHeaderSectionSkeleton.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.stushare.ui.theme.PrimaryGreen
import com.example.stushare.ui.theme.createShimmerBrush

@Composable
fun HomeHeaderSectionSkeleton() {
    val brush = createShimmerBrush()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(PrimaryGreen) // Giữ màu nền thật
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
    ) {
        // Avatar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Tên
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp)
                        .background(brush, RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(20.dp)
                        .background(brush, RoundedCornerShape(4.dp))
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        // Thanh tìm kiếm
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(brush, RoundedCornerShape(12.dp))
        )
    }
}