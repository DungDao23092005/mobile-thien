package com.example.stushare.feature_document_detail.ui.detail

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flag // 🟢 Icon Báo cáo
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.Document
import com.example.stushare.features.feature_document_detail.ui.detail.DetailUiState
import com.example.stushare.features.feature_document_detail.ui.detail.DocumentDetailViewModel
import com.example.stushare.ui.theme.PrimaryGreen
import kotlinx.coroutines.flow.collectLatest
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onBackClick: () -> Unit,
    onLoginRequired: () -> Unit,
    onReadPdf: (String, String) -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val isSendingComment by viewModel.isSendingComment.collectAsStateWithLifecycle()

    var showRatingDialog by remember { mutableStateOf(false) }
    // 🟢 MỚI: Trạng thái hiển thị hộp thoại báo cáo
    var showReportDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val isLoggedIn = currentUser != null

    LaunchedEffect(key1 = documentId) {
        viewModel.getDocumentById(documentId)
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chi tiết tài liệu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay về")
                    }
                },
                actions = {
                    // 🟢 Nút Báo cáo (Report)
                    IconButton(onClick = {
                        if (isLoggedIn) showReportDialog = true
                        else onLoginRequired()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = "Báo cáo",
                            tint = Color.Red // Màu đỏ cảnh báo
                        )
                    }

                    IconButton(onClick = {
                        if (isLoggedIn) {
                            if (uiState is DetailUiState.Success) {
                                val doc = (uiState as DetailUiState.Success).document
                                viewModel.onBookmarkClick(doc.id)
                            }
                        } else {
                            onLoginRequired()
                        }
                    }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Lưu",
                            tint = if (isBookmarked) PrimaryGreen else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState is DetailUiState.Success) {
                val document = (uiState as DetailUiState.Success).document

                Column {
                    CommentInputSection(
                        isLoggedIn = isLoggedIn,
                        isSending = isSendingComment,
                        onSendComment = { content -> viewModel.sendComment(document.id, content) },
                        onLoginRequired = onLoginRequired
                    )

                    BottomActionSection(
                        onDownloadClick = {
                            if (isLoggedIn) viewModel.startDownload(document.id, document.fileUrl, document.title, document.authorId)
                            else onLoginRequired()
                        },
                        onReadClick = {
                            if (isLoggedIn) {
                                if (document.fileUrl.isNotBlank()) onReadPdf(document.fileUrl, document.title)
                                else Toast.makeText(context, "Lỗi file", Toast.LENGTH_SHORT).show()
                            } else onLoginRequired()
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(Color.White).padding(paddingValues)) {
            when (val state = uiState) {
                is DetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryGreen) }
                is DetailUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = Color.Red) }
                is DetailUiState.Success -> {
                    DocumentDetailContentWithComments(
                        document = state.document,
                        comments = comments,
                        onRatingClick = {
                            if (isLoggedIn) showRatingDialog = true
                            else onLoginRequired()
                        },
                        currentUserId = viewModel.currentUserId,
                        onDeleteComment = { commentId ->
                            viewModel.deleteComment(state.document.id, commentId)
                        }
                    )
                }
            }
        }

        // Hộp thoại đánh giá
        if (showRatingDialog && uiState is DetailUiState.Success) {
            val doc = (uiState as DetailUiState.Success).document
            RatingDialog(
                onDismiss = { showRatingDialog = false },
                onRate = { rating ->
                    viewModel.onRateDocument(doc.id, rating)
                    showRatingDialog = false
                }
            )
        }

        // 🟢 MỚI: Hộp thoại báo cáo
        if (showReportDialog && uiState is DetailUiState.Success) {
            val doc = (uiState as DetailUiState.Success).document
            ReportDialog(
                onDismiss = { showReportDialog = false },
                onSubmit = { reason ->
                    viewModel.onReportDocument(doc.id, doc.title, reason)
                    showReportDialog = false
                }
            )
        }
    }
}

// 🟢 MỚI: Composable ReportDialog tách riêng
@Composable
fun ReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val reasons = listOf(
        "Nội dung sai sự thật/Không chính xác",
        "Vi phạm bản quyền",
        "Nội dung phản cảm/Spam",
        "Tài liệu bị lỗi không xem được",
        "Khác"
    )
    // Mặc định chọn lý do đầu tiên
    var selectedReason by remember { mutableStateOf(reasons[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Báo cáo tài liệu", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Vui lòng chọn lý do:", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                reasons.forEach { reason ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (reason == selectedReason),
                                onClick = { selectedReason = reason }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (reason == selectedReason),
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = Color.Red)
                        )
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(selectedReason) },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Gửi báo cáo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DocumentDetailContentWithComments(
    document: Document,
    comments: List<CommentEntity>,
    onRatingClick: () -> Unit,
    currentUserId: String?,
    onDeleteComment: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = document.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.width(160.dp).aspectRatio(0.7f).clip(RoundedCornerShape(16.dp)).background(Color.LightGray.copy(alpha = 0.2f)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(document.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Tác giả: ${document.author}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick = onRatingClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    BadgeInfo(Icons.Filled.Star, "%.1f".format(document.rating), Color(0xFFFFC107))
                }

                BadgeInfo(Icons.Filled.Download, "${document.downloads} lượt tải", PrimaryGreen)
                BadgeInfo(Icons.Default.ChatBubbleOutline, "${comments.size} bình luận", Color.Gray)
            }
            Spacer(Modifier.height(24.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(Modifier.height(24.dp))
            DetailSection("Mô tả tài liệu", document.description.ifBlank { "Chưa có mô tả." })
            DetailSection("Thông tin thêm", "• Mã môn: ${document.courseCode}\n• Loại: ${document.type}")
            Spacer(Modifier.height(24.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))
            Text("Bình luận (${comments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (comments.isEmpty()) {
            item {
                Text("Chưa có bình luận nào.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
            }
        } else {
            items(comments) { comment ->
                CommentItem(
                    comment = comment,
                    isOwnComment = comment.userId == currentUserId,
                    onDelete = { onDeleteComment(comment.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onRate: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đánh giá tài liệu", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Bạn thấy tài liệu này thế nào?", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                RatingBar(currentRating = 0, onRatingChanged = onRate)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng", color = Color.Gray) }
        },
        containerColor = Color.White
    )
}

@Composable
fun RatingBar(
    currentRating: Int,
    onRatingChanged: (Int) -> Unit
) {
    var rating by remember { mutableIntStateOf(currentRating) }

    Row(horizontalArrangement = Arrangement.Center) {
        for (i in 1..5) {
            IconButton(
                onClick = {
                    rating = i
                    onRatingChanged(i)
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$i Star",
                    tint = if (i <= rating) Color(0xFFFFC107) else Color.LightGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentEntity,
    isOwnComment: Boolean,
    onDelete: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        AsyncImage(
            model = comment.userAvatar ?: "https://ui-avatars.com/api/?name=${comment.userName}",
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    val dateStr = comment.timestamp?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                    } ?: "Vừa xong"
                    Text(dateStr, fontSize = 12.sp, color = Color.Gray)
                }

                if (isOwnComment) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa",
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(comment.content, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun CommentInputSection(isLoggedIn: Boolean, isSending: Boolean, onSendComment: (String) -> Unit, onLoginRequired: () -> Unit) {
    var text by remember { mutableStateOf("") }
    Surface(shadowElevation = 8.dp, color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text, onValueChange = { text = it }, placeholder = { Text("Viết bình luận...") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), maxLines = 3, enabled = !isSending,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (isLoggedIn) { if (text.isNotBlank()) { onSendComment(text); text = "" } } else onLoginRequired() })
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { if (isLoggedIn) { if (text.isNotBlank()) { onSendComment(text); text = "" } } else onLoginRequired() }, enabled = text.isNotBlank() && !isSending, modifier = Modifier.background(if (text.isNotBlank()) PrimaryGreen else Color.LightGray, CircleShape)) {
                if (isSending) CircularProgressIndicator(Modifier.size(24.dp), Color.White, 2.dp) else Icon(Icons.AutoMirrored.Filled.Send, "Gửi", tint = Color.White)
            }
        }
    }
}

@Composable
fun BadgeInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = Color.Black.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BottomActionSection(onDownloadClick: () -> Unit, onReadClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 16.dp, color = Color.White) {
        Row(modifier = Modifier.padding(20.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onReadClick, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, PrimaryGreen)) {
                Icon(Icons.Default.Visibility, null, tint = PrimaryGreen); Spacer(Modifier.width(8.dp)); Text("Đọc thử", style = MaterialTheme.typography.titleSmall, color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onDownloadClick, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen), elevation = ButtonDefaults.buttonElevation(4.dp)) {
                Icon(Icons.Filled.Download, null); Spacer(Modifier.width(8.dp)); Text("Tải về", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}