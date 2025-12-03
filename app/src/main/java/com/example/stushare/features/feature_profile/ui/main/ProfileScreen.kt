package com.example.stushare.features.feature_profile.ui.main

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.stushare.R
import com.example.stushare.features.feature_profile.ui.model.DocItem
import com.example.stushare.features.feature_profile.ui.model.UserProfile
import com.example.stushare.ui.theme.PrimaryGreen
import com.example.stushare.ui.theme.createShimmerBrush

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onDocumentClick: (String) -> Unit = {},
    onNavigateToUpload: () -> Unit,
    onNavigateToHome: () -> Unit,
    // 🟢 THÊM: Callback vào Admin
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val publishedDocs by viewModel.publishedDocuments.collectAsStateWithLifecycle()
    val savedDocs by viewModel.savedDocuments.collectAsStateWithLifecycle()
    val downloadedDocs by viewModel.downloadedDocuments.collectAsStateWithLifecycle()
    val isUploadingAvatar by viewModel.isUploadingAvatar.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.updateMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) viewModel.uploadAvatar(uri) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Crossfade(targetState = uiState, label = "ProfileState", animationSpec = tween(400)) { state ->
            when (state) {
                is ProfileUiState.Loading -> ProfileSkeleton()
                is ProfileUiState.Unauthenticated -> UnauthenticatedProfileContent(onNavigateToLogin, onNavigateToRegister)
                is ProfileUiState.Authenticated -> AuthenticatedProfileContent(
                    userProfile = state.profile,
                    totalDocs = state.totalDocs,
                    totalDownloads = state.totalDownloads,
                    memberRank = state.memberRank,
                    publishedDocs = publishedDocs,
                    savedDocs = savedDocs,
                    downloadedDocs = downloadedDocs,
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshData() },
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToLeaderboard = onNavigateToLeaderboard,
                    onDeleteDoc = { docId -> viewModel.deletePublishedDocument(docId) },
                    onAvatarClick = { singlePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onDocumentClick = onDocumentClick,
                    onNavigateToUpload = onNavigateToUpload,
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToAdmin = onNavigateToAdmin // 🟢 Truyền xuống
                )
            }
        }

        if (isUploadingAvatar) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AuthenticatedProfileContent(
    userProfile: UserProfile,
    totalDocs: Int,
    totalDownloads: Int,
    memberRank: String,
    publishedDocs: List<DocItem>,
    savedDocs: List<DocItem>,
    downloadedDocs: List<DocItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onDeleteDoc: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAdmin: () -> Unit // 🟢 Nhận callback
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(R.string.profile_tab_posted),
        stringResource(R.string.profile_tab_saved),
        stringResource(R.string.profile_tab_downloaded)
    )
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onRefresh)

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize()) {
            val userName = stringResource(R.string.profile_hello, userProfile.fullName)
            val displayMajor = if (userProfile.major.isNotBlank() && userProfile.major != "Chưa cập nhật") userProfile.major else stringResource(R.string.profile_dept)

            ProfileHeader(userName, displayMajor, userProfile.avatarUrl, onSettingsClick = onNavigateToSettings, onLeaderboardClick = onNavigateToLeaderboard, onAvatarClick = onAvatarClick)

            // 🟢 NÚT ADMIN (Chỉ hiện nếu role là admin)
            if (userProfile.role == "admin") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onNavigateToAdmin() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), // Đỏ nhạt
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TRUY CẬP TRANG QUẢN TRỊ", fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                }
            }

            StatisticsRow(totalDocs, totalDownloads, memberRank)
            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            TabRow(selectedTabIndex, containerColor = MaterialTheme.colorScheme.surface, contentColor = PrimaryGreen, indicator = { TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(it[selectedTabIndex]), color = PrimaryGreen) }) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal, color = if (selectedTabIndex == index) PrimaryGreen else MaterialTheme.colorScheme.onSurface) })
                }
            }

            Crossfade(targetState = selectedTabIndex, animationSpec = tween(300), label = "TabContent") { tabIndex ->
                val currentList = when (tabIndex) { 0 -> publishedDocs; 1 -> savedDocs; 2 -> downloadedDocs; else -> emptyList() }
                if (currentList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(top = 32.dp)) {
                        when (tabIndex) {
                            0 -> ProfileEmptyState("Bạn chưa đăng tài liệu nào", "Đăng tài liệu ngay", Icons.Default.CloudUpload, onNavigateToUpload)
                            1 -> ProfileEmptyState("Bạn chưa lưu tài liệu nào", "Khám phá ngay", Icons.Default.Search, onNavigateToHome)
                            2 -> ProfileEmptyState("Chưa có tài liệu tải xuống", "Tìm tài liệu", Icons.Default.Download, onNavigateToHome)
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(currentList, key = { it.documentId }) { doc -> DocItemRow(doc, tabIndex == 0, { onDeleteDoc(doc.documentId) }, { onDocumentClick(doc.documentId) }) }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
        PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter), contentColor = PrimaryGreen)
    }
}

// ... (Các component con ở dưới giữ nguyên như file cũ, không thay đổi)
@Composable
fun ProfileEmptyState(message: String, buttonText: String, icon: ImageVector, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(80.dp), tint = PrimaryGreen.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen), shape = RoundedCornerShape(20.dp)) { Text(buttonText) }
    }
}

@Composable
fun StatisticsRow(totalDocs: Int, totalDownloads: Int, memberRank: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().background(Color.White).padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        StatItem(count = totalDocs.toString(), label = "Tài liệu")
        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.LightGray.copy(alpha = 0.5f))
        StatItem(count = totalDownloads.toString(), label = "Lượt tải")
        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.LightGray.copy(alpha = 0.5f))
        StatItem(count = memberRank, label = "Hạng", isRank = true)
    }
}

@Composable
fun StatItem(count: String, label: String, isRank: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, style = if (isRank) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isRank) Color(0xFFFF9800) else PrimaryGreen)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun UnauthenticatedProfileContent(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.AccountCircle, null, Modifier.size(120.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        Spacer(Modifier.height(24.dp))
        Text("Bạn chưa đăng nhập", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Đăng nhập để quản lý tài liệu, xem lịch sử tải xuống và tham gia bảng xếp hạng.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Spacer(Modifier.height(32.dp))
        Button(onLoginClick, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(25.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) { Text("Đăng Nhập Ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onRegisterClick, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(25.dp), border = BorderStroke(1.dp, PrimaryGreen)) { Text("Tạo tài khoản mới", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen) }
    }
}

@Composable
fun ProfileHeader(userName: String, subText: String, avatarUrl: String?, onSettingsClick: () -> Unit, onLeaderboardClick: () -> Unit, onAvatarClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(0.dp), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.padding(bottom = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).clickable { onAvatarClick() }) {
                if (avatarUrl != null) AsyncImage(model = avatarUrl, contentDescription = "Avatar", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Image(painter = painterResource(id = R.drawable.ic_person), contentDescription = null, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)), modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(userName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(subText, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                Surface(onClick = onLeaderboardClick, shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF3E0), modifier = Modifier.wrapContentWidth()) { Text(stringResource(R.string.profile_view_leaderboard), color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
            }
            IconButton(onSettingsClick) { Icon(Icons.Default.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
        }
    }
}

@Composable
fun DocItemRow(item: DocItem, isDeletable: Boolean, onDelete: () -> Unit, onClick: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClick() }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Description, null, tint = PrimaryGreen) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.docTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Text(item.meta, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
            }
            Box {
                IconButton({ showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                DropdownMenu(showMenu, { showMenu = false }, Modifier.background(MaterialTheme.colorScheme.surface)) {
                    if (isDeletable) DropdownMenuItem({ Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.onSurface) }, { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onSurface) })
                    else DropdownMenuItem({ Text(stringResource(R.string.feature_not_supported), color = MaterialTheme.colorScheme.onSurface) }, { showMenu = false })
                }
            }
        }
    }
}

@Composable
fun ProfileSkeleton() {
    val brush = createShimmerBrush()
    Column(modifier = Modifier.fillMaxSize()) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(0.dp), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.padding(bottom = 1.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(brush))
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.width(150.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(8.dp)).background(brush))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            repeat(3) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(modifier = Modifier.width(30.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).background(brush)); Spacer(Modifier.height(4.dp)); Box(modifier = Modifier.width(40.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush)) } }
        }
        Divider(color = Color.LightGray.copy(alpha = 0.3f))
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { repeat(3) { Box(modifier = Modifier.width(80.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).background(brush)) } }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(5) { Card(modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) { Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(brush)); Spacer(Modifier.width(12.dp)); Column(modifier = Modifier.weight(1f)) { Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(brush)); Spacer(Modifier.height(8.dp)); Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush)) } } } } }
    }
}