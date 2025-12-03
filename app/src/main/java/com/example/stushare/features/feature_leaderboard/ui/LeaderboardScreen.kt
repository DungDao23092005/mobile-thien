package com.example.stushare.features.feature_leaderboard.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.UserEntity
import com.example.stushare.ui.theme.GreenPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    // 1. Setup Pager (2 trang)
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // Tiêu đề 2 tab
    val titles = listOf("Tài liệu Hot", "Top Thành viên")

    // 2. Lấy dữ liệu từ ViewModel MỚI
    val topUsers by viewModel.topUsers.collectAsStateWithLifecycle()
    val topDocuments by viewModel.topDocuments.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // Giảm chiều cao chút cho cân đối
                .background(GreenPrimary)
                .padding(top = 32.dp, start = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Text("🔙", fontSize = 24.sp, color = Color.White)
                }
                Text(
                    text = "Bảng xếp hạng",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Tab Row (Thanh chuyển tab)
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GreenPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = GreenPrimary
                )
            }
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(text = title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Nội dung Pager
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> RankedDocsList(topDocuments) // Tab 0: Tài liệu
                1 -> RankedUsersList(topUsers)    // Tab 1: Thành viên
            }
        }
    }
}

// --- CÁC COMPONENT CON (Đã sửa để nhận Entity mới) ---

@Composable
fun RankedUsersList(users: List<UserEntity>) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        // itemsIndexed giúp ta lấy số thứ tự (rank) tự động: 0->1, 1->2...
        itemsIndexed(users) { index, user ->
            RankItem(
                rank = index + 1,
                title = user.fullName,
                subtitle = "${user.contributionPoints} điểm"
            )
        }
    }
}

@Composable
fun RankedDocsList(docs: List<Document>) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        itemsIndexed(docs) { index, doc ->
            RankItem(
                rank = index + 1,
                title = doc.title,
                subtitle = "${doc.author} • ${doc.downloads} lượt tải"
            )
        }
    }
}

@Composable
fun RankItem(rank: Int, title: String, subtitle: String) {
    // Màu huy chương
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Vàng
        2 -> Color(0xFFC0C0C0) // Bạc
        3 -> Color(0xFFCD7F32) // Đồng
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Số thứ tự
            Text(
                text = "$rank",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = rankColor,
                modifier = Modifier.width(40.dp)
            )

            // Nội dung chính
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
    }
}