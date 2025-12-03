package com.example.stushare.features.feature_profile.ui.account

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stushare.R
import com.example.stushare.features.feature_profile.ui.main.ProfileViewModel
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel() // 1. Inject ViewModel
) {
    val context = LocalContext.current

    // State cho các ô nhập
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    // State hiển thị mật khẩu (Ẩn/Hiện) - Nâng cao UX
    var showCurrentPass by remember { mutableStateOf(false) }
    var showNewPass by remember { mutableStateOf(false) }

    // State Loading (để vô hiệu hóa nút khi đang gửi request)
    var isLoading by remember { mutableStateOf(false) }

    // Lắng nghe thông báo từ ViewModel
    LaunchedEffect(Unit) {
        viewModel.updateMessage.collect { message ->
            isLoading = false // Tắt loading khi có kết quả
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (message == "Đổi mật khẩu thành công!") {
                onBackClick() // Tự động back về nếu thành công
            }
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        // stringResource(R.string.cp_title), // Dùng tạm chuỗi cứng nếu chưa có string
                        "Đổi mật khẩu",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 1. Mật khẩu hiện tại
                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Mật khẩu hiện tại") },
                        visualTransformation = if (showCurrentPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPass = !showCurrentPass }) {
                                Icon(if (showCurrentPass) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = PrimaryGreen,
                            focusedBorderColor = PrimaryGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Mật khẩu mới
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("Mật khẩu mới") },
                        visualTransformation = if (showNewPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPass = !showNewPass }) {
                                Icon(if (showNewPass) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = PrimaryGreen,
                            focusedBorderColor = PrimaryGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Nhập lại mật khẩu mới
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Xác nhận mật khẩu mới") },
                        visualTransformation = PasswordVisualTransformation(), // Luôn ẩn ô này
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedLabelColor = PrimaryGreen,
                            focusedBorderColor = PrimaryGreen
                        ),
                        isError = confirmPass.isNotEmpty() && confirmPass != newPass // Báo đỏ nếu không khớp
                    )
                    if (confirmPass.isNotEmpty() && confirmPass != newPass) {
                        Text("Mật khẩu không khớp", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Button Lưu
                    Button(
                        onClick = {
                            if (currentPass.isEmpty() || newPass.isEmpty()) {
                                Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                            } else if (newPass.length < 6) {
                                Toast.makeText(context, "Mật khẩu mới phải từ 6 ký tự", Toast.LENGTH_SHORT).show()
                            } else if (newPass != confirmPass) {
                                Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                            } else {
                                isLoading = true
                                viewModel.changePassword(currentPass, newPass)
                            }
                        },
                        enabled = !isLoading, // Khóa nút khi đang load
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Đổi mật khẩu", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}