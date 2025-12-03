package com.example.stushare.features.feature_home.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.stushare.core.data.models.Document

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridDocumentCard( // Đổi tên
    document: Document,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier, // Grid sẽ tự quyết định kích thước
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = document.imageUrl,
                contentDescription = document.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f / 1.2f) // Tỷ lệ ảnh
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))

            // --- PHẦN TEXT MỚI ---
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tên tác giả: ${document.author}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Red, // (Màu đỏ giống Figma)
                maxLines = 1
            )
            Text(
                text = "Mã môn học: ${document.courseCode}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Red, // (Màu đỏ giống Figma)
                maxLines = 1
            )
            Text(
                text = "Loại: ${document.type}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Thông số (chia làm 2 hàng)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Star, "Rating", Modifier.size(16.dp), Color(0xFFFFC107))
                Spacer(Modifier.width(4.dp))
                Text(document.rating.toString(), style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Download, "Tải về", Modifier.size(16.dp), Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(document.downloads.toString(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}