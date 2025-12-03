package com.example.stushare.features.auth.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stushare.core.navigation.NavRoute
import com.google.firebase.auth.FirebaseAuth

// Định nghĩa màu (nếu chưa có trong theme)
val MauXanhProfile = Color(0xFF4CAF50) // Đã có trong file khác cùng package hoặc import từ Theme

@Composable
fun ManHinhCaNhan(navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val user = firebaseAuth.currentUser
    val daDangNhap = user != null

    // ⭐️ CẢI TIẾN QUAN TRỌNG: Tắt sóng dưới (hienThiSongDuoi = false)
    // Để tránh xung đột hình ảnh với thanh Bottom Bar màu xanh ở Main Activity
    NenHinhSong(hienThiSongDuoi = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (daDangNhap) {
                // === TRẠNG THÁI: ĐÃ ĐĂNG NHẬP ===
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MauXanhProfile
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Xin chào,", fontSize = 20.sp, color = Color.Gray)

                Text(
                    text = user?.displayName ?: user?.email ?: "Sinh viên",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (user?.email != null) {
                    Text(text = user.email!!, fontSize = 16.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        firebaseAuth.signOut()
                        // Tải lại màn hình để chuyển về trạng thái khách
                        navController.navigate(NavRoute.Profile) {
                            popUpTo(NavRoute.Profile) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Đăng Xuất", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

            } else {
                // === TRẠNG THÁI: KHÁCH (GUEST) ===
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Bạn chưa đăng nhập",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Đăng nhập để tải tài liệu, đăng bài hỏi đáp và tham gia cộng đồng UTH.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Nút Đăng Nhập
                Button(
                    onClick = { navController.navigate(NavRoute.Login) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MauXanhProfile),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đăng Nhập Ngay", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nút Đăng Ký
                OutlinedButton(
                    onClick = { navController.navigate(NavRoute.Register) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MauXanhProfile)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MauXanhProfile)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tạo tài khoản mới", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MauXanhProfile)
                }
            }
        }
    }
}