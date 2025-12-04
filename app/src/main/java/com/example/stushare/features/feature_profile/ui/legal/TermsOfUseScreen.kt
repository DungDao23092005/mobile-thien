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
fun TermsOfUseScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Điều khoản sử dụng", color = Color.White, fontWeight = FontWeight.Bold) },
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
                text = "Cập nhật lần cuối: 01/12/2025",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LegalSection(
                title = "1. Giới thiệu",
                content = "Chào mừng bạn đến với StuShare. Bằng việc truy cập và sử dụng ứng dụng này, bạn đồng ý tuân thủ các điều khoản và điều kiện dưới đây."
            )

            LegalSection(
                title = "2. Tài khoản người dùng",
                content = "Bạn chịu trách nhiệm bảo mật thông tin tài khoản của mình. Mọi hoạt động diễn ra dưới tài khoản của bạn là trách nhiệm của bạn. StuShare có quyền khóa tài khoản nếu phát hiện hành vi vi phạm."
            )

            LegalSection(
                title = "3. Chia sẻ tài liệu",
                content = "StuShare là nền tảng chia sẻ tài liệu học tập. Bạn cam kết rằng:\n- Tài liệu bạn tải lên không vi phạm bản quyền.\n- Không chứa nội dung độc hại, virus, hoặc mã độc.\n- Không chứa nội dung đồi trụy, phản động hoặc vi phạm pháp luật Việt Nam."
            )

            LegalSection(
                title = "4. Quyền sở hữu trí tuệ",
                content = "Các tài liệu được chia sẻ thuộc quyền sở hữu của người tải lên hoặc tác giả gốc. StuShare không sở hữu nội dung người dùng tải lên nhưng có quyền hiển thị và phân phối trong phạm vi ứng dụng."
            )

            LegalSection(
                title = "5. Thay đổi điều khoản",
                content = "Chúng tôi có quyền thay đổi các điều khoản này bất cứ lúc nào. Việc bạn tiếp tục sử dụng ứng dụng sau khi có thay đổi đồng nghĩa với việc bạn chấp nhận các thay đổi đó."
            )
            
            // Khoảng trống dưới cùng để không bị cấn nút
            Spacer(modifier = Modifier.padding(bottom = 32.dp))
        }
    }
}

@Composable
fun LegalSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 24.sp
        )
    }
}