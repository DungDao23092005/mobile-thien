package com.example.stushare.features.feature_notification.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // 🟢 Import quan trọng
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.R
import com.example.stushare.core.data.models.NotificationEntity
import com.example.stushare.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (NotificationUIModel) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        // 🟢 Tiêu đề đa ngôn ngữ
                        Text(
                            text = stringResource(R.string.noti_screen_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        if (unreadCount > 0) {
                            // 🟢 Số lượng tin chưa đọc đa ngôn ngữ
                            Text(
                                text = stringResource(R.string.noti_unread_count, unreadCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.content_desc_back), 
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        // 🟢 Nút Đọc tất cả đa ngôn ngữ
                        Text(
                            stringResource(R.string.noti_mark_all_read), 
                            color = Color.White, 
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen, titleContentColor = Color.White)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = notifications, key = { it.id }) { notification ->
                    SwipeToDeleteContainer(
                        item = notification,
                        onDelete = { viewModel.deleteNotification(notification.id) }
                    ) {
                        NotificationItemRow(
                            item = notification,
                            onItemClick = {
                                viewModel.markAsRead(notification.id)
                                onNotificationClick(notification)
                            },
                            onDeleteClick = { viewModel.deleteNotification(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))
            // 🟢 Empty State đa ngôn ngữ
            Text(
                stringResource(R.string.noti_empty), 
                style = MaterialTheme.typography.titleMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(item: Any, onDelete: () -> Unit, content: @Composable () -> Unit) {
    var isRemoved by remember { mutableStateOf(false) }
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                onDelete()
                true
            } else false
        }
    )
    AnimatedVisibility(visible = !isRemoved, exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()) {
        SwipeToDismissBox(
            state = state,
            backgroundContent = {
                val color = Color.Red.copy(alpha = 0.8f)
                if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(color).padding(horizontal = 24.dp), contentAlignment = Alignment.CenterEnd) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = stringResource(R.string.content_desc_delete), 
                            tint = Color.White
                        )
                    }
                }
            },
            content = { content() },
            enableDismissFromStartToEnd = false
        )
    }
}

// 🟢 Hàm helper để format thời gian theo ngôn ngữ máy
@Composable
fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 0 -> stringResource(R.string.time_just_now)
        diff < 60 * 1000 -> stringResource(R.string.time_just_now)
        diff < 60 * 60 * 1000 -> stringResource(R.string.time_minutes_ago, diff / (60 * 1000))
        diff < 24 * 60 * 60 * 1000 -> stringResource(R.string.time_hours_ago, diff / (60 * 60 * 1000))
        diff < 7 * 24 * 60 * 60 * 1000 -> stringResource(R.string.time_days_ago, diff / (24 * 60 * 60 * 1000))
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(timestamp)
        }
    }
}

@Composable
fun NotificationItemRow(item: NotificationUIModel, onItemClick: () -> Unit, onDeleteClick: () -> Unit) {
    val backgroundColor = if (!item.isRead) PrimaryGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    val contentColor = MaterialTheme.colorScheme.onSurface

    val (iconVector, iconColor) = when (item.type) {
        NotificationEntity.TYPE_UPLOAD -> Icons.Default.CheckCircle to PrimaryGreen
        NotificationEntity.TYPE_DOWNLOAD -> Icons.Default.Download to Color(0xFF2196F3)
        NotificationEntity.TYPE_RATING -> Icons.Default.Star to Color(0xFFFFC107)
        else -> Icons.Default.Notifications to Color.Gray
    }

    Card(
        onClick = onItemClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!item.isRead) 2.dp else 0.5.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(text = item.title, style = MaterialTheme.typography.titleMedium, fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Medium, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    
                    // 🟢 Dùng hàm getRelativeTime để hiển thị thời gian
                    Text(
                        text = getRelativeTime(item.timestamp), 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.Gray, 
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.message, style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp).padding(start = 4.dp).align(Alignment.CenterVertically)) {
                Icon(
                    imageVector = Icons.Default.Delete, 
                    contentDescription = stringResource(R.string.content_desc_delete), 
                    tint = Color.Gray.copy(alpha = 0.5f), 
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}