package com.example.stushare.feature_request.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.features.feature_request.ui.list.RequestListViewModel
import com.example.stushare.ui.theme.LightGreen
import com.example.stushare.ui.theme.PrimaryGreen
import com.example.stushare.ui.theme.createShimmerBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestListScreen(
    onBackClick: () -> Unit,
    onCreateRequestClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: RequestListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // 🔴 FIX: Màu nền tổng thể theo theme
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRequestClick,
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo yêu cầu mới")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 🔴 FIX: Xóa màu nền cứng Color(0xFFF9F9F9) để dùng màu nền Scaffold ở trên
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(PrimaryGreen)
                    .statusBarsPadding()
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay về",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Cộng đồng hỏi đáp",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // List Content
            when {
                uiState.isLoading -> {
                    RequestListSkeleton()
                }
                uiState.requests.isEmpty() -> {
                    EmptyRequestState(onCreateClick = onCreateRequestClick)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.requests) { request ->
                            RequestCard(
                                request = request,
                                onReplyClick = { onNavigateToDetail(request.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    request: DocumentRequest,
    onReplyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        // 🔴 FIX: Màu nền thẻ: Trắng (Light) / Xám tối (Dark)
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (request.subject.isNotBlank()) {
                Surface(
                    color = LightGreen, // Giữ nguyên màu xanh nhạt thương hiệu
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = request.subject,
                        color = PrimaryGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = request.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                // 🔴 FIX: Màu chữ tiêu đề tự động theo theme
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (request.description.isNotBlank()) {
                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodyMedium,
                    // 🔴 FIX: Màu chữ mô tả (xám hơn tiêu đề)
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 🔴 FIX: Divider màu chuẩn theme
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        // 🔴 FIX: Icon xám theo theme
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = request.authorName.ifBlank { "Sinh viên ẩn danh" },
                        style = MaterialTheme.typography.labelMedium,
                        // 🔴 FIX: Tên tác giả xám theo theme
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onReplyClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(50),
                    // 🔴 FIX: Màu nút "Trả lời"
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant, // Xám nhạt (Light) / Xám đậm (Dark)
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trả lời", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun EmptyRequestState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = PrimaryGreen.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Chưa có câu hỏi nào",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            // 🔴 FIX: Màu chữ
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hãy là người đầu tiên đặt câu hỏi để nhận được sự trợ giúp từ cộng đồng!",
            style = MaterialTheme.typography.bodyMedium,
            // 🔴 FIX: Màu chữ phụ
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tạo yêu cầu ngay")
        }
    }
}

@Composable
fun RequestListSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(5) { RequestCardSkeleton() }
    }
}

@Composable
fun RequestCardSkeleton() {
    val brush = createShimmerBrush()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        // 🔴 FIX: Nền thẻ Skeleton theo theme
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.width(100.dp).height(24.dp).clip(RoundedCornerShape(8.dp)).background(brush))
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(20.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(modifier = Modifier.height(16.dp))
            // 🔴 FIX: Màu divider trong skeleton
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Box(modifier = Modifier.width(60.dp).height(32.dp).clip(RoundedCornerShape(50)).background(brush))
            }
        }
    }
}