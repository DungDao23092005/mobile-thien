package com.example.stushare.features.feature_profile.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.stushare.R
import com.example.stushare.core.data.models.UserEntity
import com.example.stushare.features.feature_profile.ui.main.ProfileUiState
import com.example.stushare.features.feature_profile.ui.main.ProfileViewModel
import com.example.stushare.features.feature_profile.ui.model.UserProfile
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchAccountScreen(
    onBackClick: () -> Unit,
    onAddAccountClick: (String?) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val otherAccounts by viewModel.otherAccounts.collectAsStateWithLifecycle()

    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        // 🟢 Đã có: Tiêu đề màn hình
                        text = stringResource(R.string.switch_account),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // --- PHẦN 1: TÀI KHOẢN HIỆN TẠI ---
            Text(
                // 🟢 Đã sửa: "Đang hoạt động" (Section Header)
                text = stringResource(R.string.switch_acc_current_section),
                style = MaterialTheme.typography.labelLarge,
                color = onSurfaceColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            when (val state = uiState) {
                is ProfileUiState.Authenticated -> {
                    CurrentAccountCard(
                        userProfile = state.profile,
                        surfaceColor = surfaceColor,
                        onSurfaceColor = onSurfaceColor
                    )
                }
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (state is ProfileUiState.Loading) CircularProgressIndicator(color = PrimaryGreen)
                            else Text("Vui lòng đăng nhập", color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- PHẦN 2: DANH SÁCH TÀI KHOẢN CŨ ---
            if (otherAccounts.isNotEmpty()) {
                Text(
                    // 🟢 Đã sửa: "Tài khoản đã lưu"
                    text = stringResource(R.string.switch_acc_saved_section),
                    style = MaterialTheme.typography.labelLarge,
                    color = onSurfaceColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(otherAccounts) { oldUser ->
                        StoredAccountCard(
                            user = oldUser,
                            surfaceColor = surfaceColor,
                            onSurfaceColor = onSurfaceColor,
                            onClick = {
                                viewModel.signOut()
                                onAddAccountClick(oldUser.email) 
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- PHẦN 3: NÚT THÊM TÀI KHOẢN MỚI ---
            Text(
                // 🟢 Đã sửa: "Tùy chọn"
                text = stringResource(R.string.switch_acc_options_section),
                style = MaterialTheme.typography.labelLarge,
                color = onSurfaceColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.signOut()
                        onAddAccountClick(null)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = onSurfaceColor
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        // 🟢 Đã sửa: "Đăng nhập tài khoản khác"
                        text = stringResource(R.string.switch_acc_login_other),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentAccountCard(
    userProfile: UserProfile,
    surfaceColor: Color,
    onSurfaceColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!userProfile.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userProfile.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = onSurfaceColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userProfile.fullName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Text(
                    text = userProfile.email,
                    fontSize = 13.sp,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    // 🟢 Đã sửa: "Active" badge
                    text = stringResource(R.string.switch_acc_active),
                    fontSize = 12.sp,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StoredAccountCard(
    user: UserEntity,
    surfaceColor: Color,
    onSurfaceColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!user.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = onSurfaceColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = onSurfaceColor.copy(alpha = 0.8f)
                )
                Text(
                    text = user.email,
                    fontSize = 12.sp,
                    color = onSurfaceColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}