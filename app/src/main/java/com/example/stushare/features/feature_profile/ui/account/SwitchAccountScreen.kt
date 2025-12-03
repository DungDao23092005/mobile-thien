package com.example.stushare.features.feature_profile.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.stushare.features.feature_profile.ui.main.ProfileUiState
import com.example.stushare.features.feature_profile.ui.main.ProfileViewModel
import com.example.stushare.features.feature_profile.ui.model.UserProfile
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchAccountScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // 1. 🟢 LẮNG NGHE UI STATE (Thay vì userProfile cũ)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Màu động từ Theme
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.switch_acc_header), // "Chuyển tài khoản"
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // --- Phần 1: Tài khoản hiện tại ---
                Text(
                    text = stringResource(R.string.switch_acc_active), // "Đang hoạt động"
                    style = MaterialTheme.typography.labelLarge,
                    color = onSurfaceColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                // Kiểm tra State để hiển thị thông tin
                when (val state = uiState) {
                    is ProfileUiState.Loading -> {
                        // Hiển thị Loading Card
                        Card(
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryGreen)
                            }
                        }
                    }
                    is ProfileUiState.Authenticated -> {
                        // Hiển thị Card thông tin User thật
                        CurrentAccountCard(
                            userProfile = state.profile,
                            surfaceColor = surfaceColor,
                            onSurfaceColor = onSurfaceColor
                        )
                    }
                    else -> {
                        // Trường hợp Unauthenticated (Hiếm khi xảy ra ở màn này)
                        Text("Vui lòng đăng nhập lại", color = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Phần 2: Thêm tài khoản khác ---
                Text(
                    text = stringResource(R.string.switch_account), // "Tài khoản khác"
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
                            // ⭐️ LOGIC: Đăng xuất để về màn hình Login -> Nhập tk khác
                            viewModel.signOut()
                            // Điều hướng do MainActivity/NavGraph xử lý khi authState thay đổi
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
                            text = stringResource(R.string.switch_acc_add), // "Đăng nhập tài khoản khác"
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceColor
                        )
                    }
                }
            }

            // Bottom Curve (Trang trí)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(100.dp)
                    .offset(y = 50.dp)
                    .background(
                        color = PrimaryGreen,
                        shape = RoundedCornerShape(topStart = 1000.dp, topEnd = 1000.dp)
                    )
            )
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
            // ⭐️ CẢI TIẾN: Hiển thị Avatar thật dùng Coil
            if (userProfile.avatarUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userProfile.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder nếu chưa có ảnh
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

            // Thông tin User
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
                    text = stringResource(R.string.switch_acc_active), // "Đang hoạt động"
                    fontSize = 12.sp,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            // Icon check xanh
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}