package com.example.stushare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // 1. HEADLINE (Dùng cho tên người dùng, tiêu đề lớn nhất trang)
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // 2. TITLE LARGE (Dùng cho tiêu đề Section: "Mới tải lên", Header màn hình)
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp, // Tăng độ đậm
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // 3. TITLE MEDIUM (Dùng cho tên Tài liệu trong Card)
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Đậm vừa phải để dễ đọc tên sách
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    // 4. TITLE SMALL (Dùng cho các mục phụ, sub-header)
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // 5. BODY LARGE (Dùng cho lời chào "Xin chào", hoặc văn bản chính)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    // 6. BODY MEDIUM (Nội dung phụ, mô tả ngắn)
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),

    // 7. LABEL SMALL (Caption, số lượt tải, rating)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp, // Nhỏ gọn tinh tế
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)