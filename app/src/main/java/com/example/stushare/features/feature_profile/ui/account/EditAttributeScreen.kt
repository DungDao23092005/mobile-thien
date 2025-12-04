package com.example.stushare.features.feature_profile.ui.account

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stushare.R
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAttributeScreen(
    title: String,
    initialValue: String,
    label: String,
    onBackClick: () -> Unit,
    onSaveClick: (String) -> Unit, // 🟢 Callback trả dữ liệu về
    keyboardType: KeyboardType = KeyboardType.Text // 🟢 [Mới] Để hỗ trợ bàn phím số khi nhập SĐT
) {
    val context = LocalContext.current
    var value by remember { mutableStateOf(initialValue) }

    // Lấy chuỗi resource để hiển thị thông báo
    val errEmpty = stringResource(R.string.err_input_empty)

    // Dynamic theme colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back), // [Sửa] Đa ngôn ngữ cho nút Back
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, // [Sửa] Thường input 1 dòng nên set true
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType, // [Sửa] Sử dụng loại bàn phím được truyền vào
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurfaceColor,
                    unfocusedTextColor = onSurfaceColor,
                    focusedLabelColor = PrimaryGreen,
                    focusedBorderColor = PrimaryGreen
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (value.isNotBlank()) {
                        // 🟢 Call the callback with the new value
                        onSaveClick(value)
                    } else {
                        // [Sửa] Sử dụng thông báo đa ngôn ngữ
                        Toast.makeText(context, errEmpty, Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                // [Đã có sẵn] String resource cho nút Lưu
                Text(stringResource(R.string.edit_save_btn))
            }
        }
    }
}