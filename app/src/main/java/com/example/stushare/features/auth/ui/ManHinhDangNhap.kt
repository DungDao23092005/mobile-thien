package com.example.stushare.features.auth.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stushare.R
import com.example.stushare.core.navigation.NavRoute
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

// --- ĐỊNH NGHĨA MÀU TRỰC TIẾP (Không cần import PrimaryGreen) ---
val MauXanhDangNhap = Color(0xFF4CAF50)

@Composable
fun ManHinhDangNhap(boDieuHuong: NavController) {
    // Các biến trạng thái
    var email by remember { mutableStateOf("") }
    var matKhau by remember { mutableStateOf("") }
    var hienThiMatKhau by remember { mutableStateOf(false) }
    var dangXuLy by remember { mutableStateOf(false) }
    var thongBaoLoi by remember { mutableStateOf("") }

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    // Hàm xử lý đăng nhập
    fun thucHienDangNhap() {
        thongBaoLoi = ""
        if (email.isEmpty() || matKhau.isEmpty()) {
            thongBaoLoi = "Vui lòng nhập đầy đủ thông tin"
            return
        }

        dangXuLy = true
        firebaseAuth.signInWithEmailAndPassword(email, matKhau)
            .addOnCompleteListener { tacVu ->
                dangXuLy = false
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                    // --- ĐIỀU HƯỚNG VỀ HOME ---
                    boDieuHuong.navigate(NavRoute.Home) {
                        popUpTo(NavRoute.Login) { inclusive = true }
                    }
                } else {
                    val ngoaiLe = tacVu.exception
                    thongBaoLoi = when (ngoaiLe) {
                        is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại."
                        is FirebaseAuthInvalidCredentialsException -> "Sai email hoặc mật khẩu."
                        else -> "Lỗi: ${ngoaiLe?.message}"
                    }
                }
            }
    }

    // Đảm bảo file NenHinhSong.kt đã sửa package đúng
    NenHinhSong {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Đăng Nhập",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MauXanhDangNhap
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Input Email
            TextField(
                value = email,
                onValueChange = {
                    email = it
                    thongBaoLoi = ""
                },
                placeholder = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, if (thongBaoLoi.isNotEmpty()) Color.Red else Color.Transparent, RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color(0xFFE0E0E0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input Mật khẩu
            TextField(
                value = matKhau,
                onValueChange = {
                    matKhau = it
                    thongBaoLoi = ""
                },
                placeholder = { Text("Mật khẩu") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    IconButton(onClick = { hienThiMatKhau = !hienThiMatKhau }) {
                        Icon(
                            imageVector = if (hienThiMatKhau) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, if (thongBaoLoi.isNotEmpty()) Color.Red else Color.Transparent, RoundedCornerShape(16.dp)),
                visualTransformation = if (hienThiMatKhau) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color(0xFFE0E0E0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Hiển thị lỗi
            if (thongBaoLoi.isNotEmpty()) {
                Text(
                    text = thongBaoLoi,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quên mật khẩu
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "Quên mật khẩu?",
                    color = MauXanhDangNhap,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        boDieuHuong.navigate(NavRoute.ForgotPassword)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút Đăng nhập
            Button(
                onClick = { thucHienDangNhap() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauXanhDangNhap),
                shape = RoundedCornerShape(25.dp),
                enabled = !dangXuLy
            ) {
                if (dangXuLy) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Đăng Nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Đăng nhập bằng cách khác
            Text("Hoặc đăng nhập bằng", color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nút Google
                IconButton(
                    onClick = { /* TODO: Google Sign In */ },
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Nút Số điện thoại
                IconButton(
                    onClick = { boDieuHuong.navigate(NavRoute.LoginSMS) },
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_dienthoai1),
                        contentDescription = "Phone",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Chuyển sang Đăng ký
            Row {
                Text("Chưa có tài khoản? ", color = Color.Black)
                Text(
                    text = "Đăng ký ngay",
                    color = MauXanhDangNhap,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        boDieuHuong.navigate(NavRoute.Register)
                    }
                )
            }
        }
    }
}