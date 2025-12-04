package com.example.stushare.features.feature_profile.ui.legal

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stushare.ui.theme.PrimaryGreen

// Data Class cho câu hỏi
data class FAQItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSupportScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // --- DANH SÁCH CÂU HỎI THƯỜNG GẶP ---
    val faqList = listOf(
        FAQItem(
            question = "Làm thế nào để tải tài liệu lên?",
            answer = "Tại màn hình Trang chủ, bạn nhấn vào nút '+' hoặc biểu tượng Tải lên (Upload). Sau đó chọn file PDF từ máy, điền thông tin mô tả và nhấn 'Đăng ngay'."
        ),
        FAQItem(
            question = "Tôi quên mật khẩu thì phải làm sao?",
            answer = "Tại màn hình Đăng nhập, hãy chọn 'Quên mật khẩu?'. Hệ thống sẽ gửi email hướng dẫn đặt lại mật khẩu cho bạn."
        ),
        FAQItem(
            question = "Tại sao tài liệu của tôi bị từ chối?",
            answer = "Tài liệu có thể bị từ chối nếu vi phạm bản quyền, chứa nội dung không phù hợp, chất lượng quá thấp hoặc không đúng định dạng PDF."
        ),
        FAQItem(
            question = "Làm sao để báo cáo nội dung xấu?",
            answer = "Trong trang chi tiết tài liệu, nhấn vào biểu tượng '...' ở góc trên và chọn 'Báo cáo vi phạm'. Chúng tôi sẽ xem xét trong vòng 24h."
        ),
        FAQItem(
            question = "Tôi có thể đổi số điện thoại không?",
            answer = "Có. Bạn vào Cài đặt -> Bảo mật tài khoản -> Nhấn vào số điện thoại để cập nhật số mới và xác thực OTP."
        ),
        FAQItem(
            question = "StuShare có thu phí không?",
            answer = "Hiện tại StuShare là nền tảng chia sẻ miễn phí cho cộng đồng học sinh, sinh viên."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liên hệ & Hỗ trợ", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // --- PHẦN 1: KÊNH LIÊN HỆ ---
            Text(
                text = "Kênh hỗ trợ trực tuyến",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Nút Gọi Hotline
                ContactCard(
                    icon = Icons.Default.Call,
                    title = "Hotline",
                    subTitle = "1900 1234",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:19001234")
                        }
                        context.startActivity(intent)
                    }
                )

                // Nút Gửi Email
                ContactCard(
                    icon = Icons.Default.Email,
                    title = "Email",
                    subTitle = "support@stushare.com",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@stushare.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ StuShare")
                        }
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- PHẦN 2: CÂU HỎI THƯỜNG GẶP (FAQ) ---
            Text(
                text = "Câu hỏi thường gặp",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Render danh sách câu hỏi
            faqList.forEach { faq ->
                FAQCard(faq = faq)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Composable: Card Liên hệ (Nút to ở trên)
@Composable
fun ContactCard(
    icon: ImageVector,
    title: String,
    subTitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryGreen)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subTitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// Composable: Item FAQ (Có thể xổ xuống)
@Composable
fun FAQCard(faq: FAQItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = faq.question,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        // Hiệu ứng xổ xuống mượt mà
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            Column {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = faq.answer,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}