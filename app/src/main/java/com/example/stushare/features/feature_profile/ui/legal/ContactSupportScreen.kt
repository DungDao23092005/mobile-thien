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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stushare.R
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

    // --- DANH SÁCH CÂU HỎI THƯỜNG GẶP (Đã dùng stringResource) ---
    val faqList = listOf(
        FAQItem(stringResource(R.string.faq_q1), stringResource(R.string.faq_a1)),
        FAQItem(stringResource(R.string.faq_q2), stringResource(R.string.faq_a2)),
        FAQItem(stringResource(R.string.faq_q3), stringResource(R.string.faq_a3)),
        FAQItem(stringResource(R.string.faq_q4), stringResource(R.string.faq_a4)),
        FAQItem(stringResource(R.string.faq_q5), stringResource(R.string.faq_a5)),
        FAQItem(stringResource(R.string.faq_q6), stringResource(R.string.faq_a6))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                // 🟢 Đã sửa: Tiêu đề lấy từ resource
                title = { Text(stringResource(R.string.support_header), color = Color.White, fontWeight = FontWeight.Bold) },
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
                // 🟢 Đã sửa: Header section 1
                text = stringResource(R.string.support_online_channel),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Nút Gọi Hotline
                ContactCard(
                    icon = Icons.Default.Call,
                    // 🟢 Đã sửa: Tiêu đề Hotline
                    title = stringResource(R.string.support_hotline_title),
                    // Số điện thoại giữ nguyên text cứng hoặc đưa vào resource nếu cần
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
                    // 🟢 Đã sửa: Tiêu đề Email
                    title = "Email", // Có thể dùng stringResource(R.string.acc_sec_email) nếu muốn
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
                // 🟢 Đã sửa: Header FAQ
                text = stringResource(R.string.faq_header),
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