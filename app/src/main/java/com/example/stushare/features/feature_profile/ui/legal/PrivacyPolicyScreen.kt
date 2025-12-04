package com.example.stushare.features.feature_profile.ui.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stushare.R
import com.example.stushare.ui.theme.PrimaryGreen
import androidx.compose.foundation.layout.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                // 🟢 Đã sửa: Tiêu đề lấy từ strings.xml
                title = { Text(stringResource(R.string.privacy_header), color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 🟢 Đã sửa: Hiển thị nội dung đầy đủ từ strings.xml (biến privacy_content)
            Text(
                text = stringResource(R.string.privacy_content),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 24.sp
            )

             Spacer(modifier = Modifier.padding(bottom = 32.dp))
        }
    }
}