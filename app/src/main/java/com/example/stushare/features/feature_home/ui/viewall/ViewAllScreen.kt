package com.example.stushare.features.feature_home.ui.viewall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.core.data.models.Document
import com.example.stushare.features.feature_home.ui.components.DocumentCard
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAllScreen(
    category: String, // Nhận category id (ví dụ: "new_uploads")
    onBackClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    viewModel: ViewAllViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 🟢 CẬP NHẬT: Dịch đầy đủ tiêu đề sang Tiếng Việt
    val screenTitle = when(category) {
        "new_uploads" -> "Mới được tải lên"
        "exam_review", "exam_prep" -> "Tài liệu ôn thi"
        "book" -> "Sách / Giáo trình" // Thêm dòng này
        "lecture", "slide" -> "Bài giảng / Slide" // Thêm dòng này
        else -> "Xem tất cả"
    }

    // Load dữ liệu khi màn hình khởi tạo
    LaunchedEffect(key1 = category) {
        viewModel.loadCategory(category)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // 🔴 FIX: Màu nền tổng thể
        // 1. Header: Clean & Simple (Đồng bộ với Detail Screen)
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay về"
                        )
                    }
                },
                actions = {
                    // Nút Lọc (Visual Cue): Gợi ý tính năng lọc/sắp xếp
                    IconButton(onClick = { /* TODO: Mở BottomSheet lọc */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Lọc",
                            // 🔴 FIX: Màu icon lọc theo theme
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                // 🔴 FIX: Màu TopBar theo theme
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 🔴 FIX: Dùng màu nền động thay vì Color.White
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ViewAllUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is ViewAllUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Đã xảy ra lỗi: ${state.message}",
                            // 🔴 FIX: Màu lỗi chuẩn theme
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is ViewAllUiState.Success -> {
                    if (state.documents.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            // 🔴 FIX: Màu chữ thông báo trống
                            Text(
                                "Không có tài liệu nào trong mục này", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // 2. Grid Layout: Tối ưu khoảng trắng (Whitespace)
                        DocumentGridContent(
                            documents = state.documents,
                            onDocumentClick = onDocumentClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentGridContent(
    documents: List<Document>,
    onDocumentClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 Cột
        contentPadding = PaddingValues(16.dp), // Padding bên ngoài (Thoáng)
        horizontalArrangement = Arrangement.spacedBy(16.dp), // Khoảng cách cột (Rộng hơn cũ)
        verticalArrangement = Arrangement.spacedBy(20.dp),   // Khoảng cách hàng
        modifier = Modifier.fillMaxSize()
    ) {
        items(documents) { document ->
            // 3. Component: Tái sử dụng DocumentCard đã nâng cấp
            // Card sẽ tự giãn chiều ngang (fillMaxWidth) theo cột của Grid
            DocumentCard(
                document = document,
                onClick = { onDocumentClick(document.id.toString()) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}