package com.example.stushare.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Định nghĩa màu Xanh chủ đạo
// (Nếu file Color.kt của bạn đã có PrimaryGreen thì xóa dòng này đi)
//val PrimaryGreen = Color(0xFF4CAF50)

// 2. Bảng màu TỐI (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.Black,
    secondary = Color(0xFF81C784),
    tertiary = Color(0xFF4DD0E1),

    // Nền tối chuẩn
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE0E0E0), // Chữ trắng xám
    onSurface = Color(0xFFE0E0E0)
)

// 3. Bảng màu SÁNG (Light Mode)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    secondary = Color(0xFF4CAF50),
    tertiary = Color(0xFF00BCD4),

    // Nền sáng chuẩn
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onBackground = Color.Black, // Chữ đen
    onSurface = Color.Black
)

@Composable
fun StuShareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // 🔴 SỬA Ở ĐÂY: Đặt thành Transparent để nội dung tràn lên được
            window.statusBarColor = Color.Transparent.toArgb() 
            
            // Chỉnh màu icon trên thanh trạng thái:
            // !darkTheme (Theme Sáng) -> Icon màu ĐEN (true)
            // darkTheme (Theme Tối) -> Icon màu TRẮNG (false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Logic phóng to chữ giữ nguyên
    val defaultTypography = Typography 

    val scaledTypography = Typography(
        displayLarge = defaultTypography.displayLarge.copy(
            fontSize = defaultTypography.displayLarge.fontSize * fontScale,
            lineHeight = defaultTypography.displayLarge.lineHeight * fontScale
        ),
        displayMedium = defaultTypography.displayMedium.copy(
            fontSize = defaultTypography.displayMedium.fontSize * fontScale,
            lineHeight = defaultTypography.displayMedium.lineHeight * fontScale
        ),
        displaySmall = defaultTypography.displaySmall.copy(
            fontSize = defaultTypography.displaySmall.fontSize * fontScale,
            lineHeight = defaultTypography.displaySmall.lineHeight * fontScale
        ),
        headlineLarge = defaultTypography.headlineLarge.copy(
            fontSize = defaultTypography.headlineLarge.fontSize * fontScale,
            lineHeight = defaultTypography.headlineLarge.lineHeight * fontScale
        ),
        headlineMedium = defaultTypography.headlineMedium.copy(
            fontSize = defaultTypography.headlineMedium.fontSize * fontScale,
            lineHeight = defaultTypography.headlineMedium.lineHeight * fontScale
        ),
        headlineSmall = defaultTypography.headlineSmall.copy(
            fontSize = defaultTypography.headlineSmall.fontSize * fontScale,
            lineHeight = defaultTypography.headlineSmall.lineHeight * fontScale
        ),
        titleLarge = defaultTypography.titleLarge.copy(
            fontSize = defaultTypography.titleLarge.fontSize * fontScale,
            lineHeight = defaultTypography.titleLarge.lineHeight * fontScale
        ),
        titleMedium = defaultTypography.titleMedium.copy(
            fontSize = defaultTypography.titleMedium.fontSize * fontScale,
            lineHeight = defaultTypography.titleMedium.lineHeight * fontScale
        ),
        titleSmall = defaultTypography.titleSmall.copy(
            fontSize = defaultTypography.titleSmall.fontSize * fontScale,
            lineHeight = defaultTypography.titleSmall.lineHeight * fontScale
        ),
        bodyLarge = defaultTypography.bodyLarge.copy(
            fontSize = defaultTypography.bodyLarge.fontSize * fontScale,
            lineHeight = defaultTypography.bodyLarge.lineHeight * fontScale
        ),
        bodyMedium = defaultTypography.bodyMedium.copy(
            fontSize = defaultTypography.bodyMedium.fontSize * fontScale,
            lineHeight = defaultTypography.bodyMedium.lineHeight * fontScale
        ),
        bodySmall = defaultTypography.bodySmall.copy(
            fontSize = defaultTypography.bodySmall.fontSize * fontScale,
            lineHeight = defaultTypography.bodySmall.lineHeight * fontScale
        ),
        labelLarge = defaultTypography.labelLarge.copy(
            fontSize = defaultTypography.labelLarge.fontSize * fontScale,
            lineHeight = defaultTypography.labelLarge.lineHeight * fontScale
        ),
        labelMedium = defaultTypography.labelMedium.copy(
            fontSize = defaultTypography.labelMedium.fontSize * fontScale,
            lineHeight = defaultTypography.labelMedium.lineHeight * fontScale
        ),
        labelSmall = defaultTypography.labelSmall.copy(
            fontSize = defaultTypography.labelSmall.fontSize * fontScale,
            lineHeight = defaultTypography.labelSmall.lineHeight * fontScale
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography, 
        content = content
    )
}