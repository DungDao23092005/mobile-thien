package com.example.stushare.feature_search.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.core.data.models.Document
import com.example.stushare.features.feature_home.ui.components.DocumentCard
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    onBackClick: () -> Unit,
    onDocumentClick: (Long) -> Unit,
    onRequestClick: () -> Unit, // Giữ lại callback này cho Empty State
    viewModel: SearchResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query = viewModel.query

    Scaffold(
        topBar = {
            SearchResultTopBar(
                query = query,
                onBackClick = onBackClick
            )
        }
        // ❌ ĐÃ XÓA FloatingActionButton ở đây theo yêu cầu
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SearchUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is SearchUiState.Success -> {
                    if (state.results.isEmpty()) {
                        // Gọi Empty State khi danh sách rỗng
                        EmptyResult(query = query, onRequestClick = onRequestClick)
                    } else {
                        SearchResultList(
                            documents = state.results,
                            onDocumentClick = onDocumentClick as (String) -> Unit
                        )
                    }
                }
                is SearchUiState.Empty -> {
                    // Gọi Empty State khi search trả về kết quả Empty
                    EmptyResult(query = query, onRequestClick = onRequestClick)
                }
                is SearchUiState.Error -> {
                    ErrorMessage(message = state.message)
                }
                is SearchUiState.Initial -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
            }
        }
    }
}

// --- Component TopBar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultTopBar(query: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Kết quả cho \"$query\"",
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
}

// --- Component Hiển thị Danh sách Kết quả ---
@Composable
private fun SearchResultList(
    documents: List<Document>,
    onDocumentClick: (String) -> Unit
) {
    LazyColumn(
        // 🛠 Đã chỉnh lại padding bottom về 16.dp (vì không còn FAB che nữa)
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Tìm thấy ${documents.size} tài liệu",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(documents) { document ->
            DocumentCard(
                document = document,
                onClick = { onDocumentClick(document.id) }
            )
        }
    }
}

// --- Component Không có Kết quả (Đã tích hợp nút Action) ---
@Composable
private fun EmptyResult(
    query: String,
    onRequestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = "Không tìm thấy",
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Rất tiếc, không tìm thấy tài liệu nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Chúng tôi không tìm thấy kết quả cho từ khóa \"$query\".",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nút kêu gọi hành động (CTA)
        Button(
            onClick = onRequestClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            modifier = Modifier.height(50.dp)
        ) {
            Text(
                text = "Nhờ cộng đồng tìm giúp ngay!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- Component Lỗi ---
@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Lỗi",
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Đã xảy ra lỗi: $message",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}