package com.example.stushare.features.auth.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

// Định nghĩa màu
val MauXanhSong = Color(0xFF2ecc71)
val MauXanhDam = Color(0xFF27ae60)

@Composable
fun NenHinhSong(
    // ⭐️ CẢI TIẾN: Thêm tham số bật/tắt sóng dưới (Mặc định là true để hiện)
    hienThiSongDuoi: Boolean = true,
    noiDung: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val chieuRong = size.width
            val chieuCao = size.height

            // --- 1. VẼ SÓNG TRÊN (Luôn luôn vẽ) ---
            val duongDanTren = Path().apply {
                moveTo(0f, 0f)
                lineTo(chieuRong, 0f)
                lineTo(chieuRong, chieuCao * 0.1f)
                cubicTo(
                    chieuRong * 0.5f, chieuCao * 0.2f,
                    chieuRong * 0.2f, chieuCao * 0.05f,
                    0f, chieuCao * 0.15f
                )
                close()
            }

            drawPath(
                path = duongDanTren,
                brush = Brush.verticalGradient(
                    colors = listOf(MauXanhDam, MauXanhSong),
                    startY = 0f,
                    endY = chieuCao * 0.25f
                )
            )

            // --- 2. VẼ SÓNG DƯỚI (Vẽ có điều kiện) ---
            // Chỉ vẽ khi hienThiSongDuoi = true.
            // Giúp tránh xung đột (đè lên nhau) với thanh NavigationBar màu xanh ở màn hình chính.
            if (hienThiSongDuoi) {
                val duongDanDuoi = Path().apply {
                    moveTo(0f, chieuCao)
                    lineTo(chieuRong, chieuCao)
                    val chieuCaoSong = chieuCao * 0.9f
                    lineTo(chieuRong, chieuCaoSong)
                    quadraticBezierTo(
                        chieuRong * 0.5f, chieuCao * 1f,
                        0f, chieuCaoSong
                    )
                    close()
                }

                drawPath(
                    path = duongDanDuoi,
                    color = MauXanhSong
                )
            }
        }

        // Hiển thị nội dung màn hình lên trên
        noiDung()
    }
}