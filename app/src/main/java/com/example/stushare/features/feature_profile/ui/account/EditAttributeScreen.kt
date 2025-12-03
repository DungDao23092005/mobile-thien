package com.example.stushare.features.feature_profile.ui.account

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// ⭐️ Import đúng Resource và Theme của dự án chính
import com.example.stushare.R
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAttributeScreen(
    title: String,
    initialValue: String,
    label: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var value by remember { mutableStateOf(initialValue) }

    // Màu động từ Theme
    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                // ⭐️ SỬA: Dùng PrimaryGreen
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurfaceColor,
                    unfocusedTextColor = onSurfaceColor,
                    // ⭐️ SỬA: Dùng PrimaryGreen
                    focusedLabelColor = PrimaryGreen,
                    focusedBorderColor = PrimaryGreen
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            val successMsg = stringResource(R.string.edit_success)
            Button(
                onClick = {
                    // TODO: Gọi ViewModel để lưu thay đổi vào DB/API
                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                    onBackClick()
                },
                // ⭐️ SỬA: Dùng PrimaryGreen
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.edit_save_btn))
            }
        }
    }
}