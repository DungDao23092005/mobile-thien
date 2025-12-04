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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stushare.ui.theme.PrimaryGreen
import androidx.compose.foundation.layout.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chính sách bảo mật", color = Color.White, fontWeight = FontWeight.Bold) },
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
            Text(
                text = "StuShare cam kết bảo vệ quyền riêng tư của bạn. Chính sách này mô tả cách chúng tôi thu thập, sử dụng và bảo vệ thông tin cá nhân của bạn.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LegalSection(
                title = "1. Thông tin chúng tôi thu thập",
                content = "- Thông tin cá nhân: Tên, địa chỉ email, ảnh đại diện khi bạn đăng ký tài khoản.\n- Dữ liệu sử dụng: Các tài liệu bạn xem, tải xuống hoặc tải lên.\n- Thông tin thiết bị: Loại thiết bị, hệ điều hành (để tối ưu hóa trải nghiệm)."
            )

            LegalSection(
                title = "2. Cách sử dụng thông tin",
                content = "Chúng tôi sử dụng thông tin của bạn để:\n- Cung cấp và duy trì dịch vụ.\n- Thông báo về các thay đổi trong dịch vụ.\n- Hỗ trợ khách hàng.\n- Phát hiện và ngăn chặn các vấn đề kỹ thuật."
            )

            LegalSection(
                title = "3. Chia sẻ thông tin",
                content = "StuShare KHÔNG bán, trao đổi hoặc chuyển giao thông tin cá nhân của bạn cho bên thứ ba. Chúng tôi chỉ chia sẻ thông tin khi có yêu cầu từ cơ quan pháp luật."
            )

            LegalSection(
                title = "4. Bảo mật dữ liệu",
                content = "Chúng tôi thực hiện các biện pháp an ninh thích hợp để bảo vệ chống lại việc truy cập trái phép hoặc sửa đổi, tiết lộ hoặc phá hủy dữ liệu của bạn."
            )

            LegalSection(
                title = "5. Liên hệ",
                content = "Nếu bạn có bất kỳ câu hỏi nào về Chính sách bảo mật này, vui lòng liên hệ với chúng tôi qua email: support@stushare.com"
            )

             Spacer(modifier = Modifier.padding(bottom = 32.dp))
        }
    }
}