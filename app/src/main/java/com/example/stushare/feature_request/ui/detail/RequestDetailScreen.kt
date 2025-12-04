package com.example.stushare.feature_request.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    onBackClick: () -> Unit,
    viewModel: RequestDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val commentText by viewModel.commentText.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // 🔴 FIX: Màu nền tổng thể
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết & Thảo luận", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // 🔴 FIX: Màu TopBar theo theme
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            // Thanh nhập chat
            Surface(
                shadowElevation = 8.dp,
                // 🔴 FIX: Nền thanh chat
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = viewModel::onCommentChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = { 
                            Text(
                                "Nhập bình luận hoặc link tài liệu...", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            ) 
                        },
                        shape = RoundedCornerShape(24.dp),
                        // 🔴 FIX: Màu TextField nhập liệu
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            viewModel.sendComment()
                            keyboardController?.hide()
                        })
                    )

                    IconButton(
                        onClick = {
                            viewModel.sendComment()
                            keyboardController?.hide()
                        },
                        enabled = commentText.isNotBlank() && !uiState.isSending
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi",
                            // 🔴 FIX: Màu nút gửi
                            tint = if (commentText.isNotBlank()) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 🔴 FIX: Xóa màu nền cứng Color(0xFFF5F7F5) -> Dùng nền theme
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (uiState.isLoadingRequest) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryGreen
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Nội dung câu hỏi (Ghim trên cùng)
                    item {
                        uiState.request?.let { request ->
                            RequestContentHeader(request)
                            Spacer(modifier = Modifier.height(8.dp))
                            // 🔴 FIX: Màu Divider
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Bình luận (${uiState.comments.size})",
                                style = MaterialTheme.typography.titleSmall,
                                // 🔴 FIX: Màu tiêu đề section
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 2. Danh sách bình luận (Chat)
                    if (uiState.comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Chưa có bình luận nào. Hãy là người đầu tiên!", 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(uiState.comments) { comment ->
                            CommentItem(
                                comment = comment,
                                isCurrentUser = comment.userId == uiState.currentUserId
                            )
                        }
                    }

                    // Khoảng trống dưới cùng để không bị thanh chat che
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }
}

@Composable
fun RequestContentHeader(request: DocumentRequest) {
    Card(
        // 🔴 FIX: Màu nền Card nội dung câu hỏi
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Môn học
            Surface(
                color = PrimaryGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = request.subject,
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                // 🔴 FIX: Màu chữ tiêu đề
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                // 🔴 FIX: Màu chữ mô tả
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp), 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Đăng bởi ${request.authorName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CommentItem(comment: CommentEntity, isCurrentUser: Boolean) {
    val dateFormatter = SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault())
    val timeString = comment.timestamp?.let { dateFormatter.format(it) } ?: "Vừa xong"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Tên người dùng (Chỉ hiện nếu không phải mình)
        if (!isCurrentUser) {
            Text(
                text = comment.userName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
            )
        }

        // Bong bóng chat
        Surface(
            shape = if (isCurrentUser)
                RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
            else
                RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            // 🔴 FIX: Màu bong bóng chat:
            // - Của mình: PrimaryGreen
            // - Của người khác: surfaceVariant (Xám nhạt ở Light, Xám đậm ở Dark)
            color = if (isCurrentUser) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp
        ) {
            Text(
                text = comment.content,
                modifier = Modifier.padding(12.dp),
                // 🔴 FIX: Màu chữ trong bong bóng
                color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Thời gian
        Text(
            text = timeString,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
        )
    }
}