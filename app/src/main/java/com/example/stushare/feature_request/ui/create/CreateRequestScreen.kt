package com.example.stushare.features.feature_request.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stushare.ui.theme.PrimaryGreen

@Composable
fun CreateRequestScreen(
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
    viewModel: CreateRequestViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val isFormValid = title.isNotBlank() && subject.isNotBlank()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // 🔴 FIX: Màu nền tổng thể
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                // 🔴 FIX: Màu nền thanh dưới cùng theo theme
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.submitRequest(title, subject, description)
                        onSubmitClick()
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .padding(20.dp)
                        .navigationBarsPadding()
                        .height(54.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White,
                        // 🔴 FIX: Màu nút khi disable cho dễ nhìn trên nền tối
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Gửi yêu cầu ngay",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 🔴 FIX: Xóa background(Color.White) cứng, dùng mặc định của Scaffold
                .padding(paddingValues)
        ) {
            // 1. HEADER MÀU XANH (Giữ nguyên vì design yêu cầu)
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
                    text = "Tạo yêu cầu mới",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 2. NỘI DUNG FORM
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Hãy mô tả chi tiết tài liệu bạn cần tìm để cộng đồng hỗ trợ nhanh nhất nhé!",
                    style = MaterialTheme.typography.bodyMedium,
                    // 🔴 FIX: Màu chữ phụ chuẩn theme
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Input: Tiêu đề
                InputGroup(
                    label = "Tiêu đề yêu cầu (*)",
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "VD: Cần tìm đề thi cuối kỳ môn Toán",
                    imeAction = ImeAction.Next
                )

                // Input: Môn học
                InputGroup(
                    label = "Môn học (*)",
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = "VD: Giải tích 1",
                    imeAction = ImeAction.Next
                )

                // Input: Mô tả
                InputGroup(
                    label = "Mô tả chi tiết",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "VD: Dành cho hệ không chuyên, năm 2023...",
                    singleLine = false,
                    minLines = 5,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }
        }
    }
}

@Composable
fun InputGroup(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            // 🔴 FIX: Màu label tự động trắng/đen
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { 
                Text(
                    placeholder, 
                    // 🔴 FIX: Màu placeholder nhạt đi theo theme
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            // 🔴 FIX: Hệ thống màu cho TextField
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface, // Nền khi focus
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // Nền khi không focus
                cursorColor = PrimaryGreen,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions
        )
    }
}