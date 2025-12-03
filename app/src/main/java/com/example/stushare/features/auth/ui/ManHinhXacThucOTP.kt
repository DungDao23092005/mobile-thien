package com.example.stushare.features.auth.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stushare.core.navigation.NavRoute // Import Route mới
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay

// --- ĐỊNH NGHĨA MÀU TRỰC TIẾP ---
val MauXanhOTP = Color(0xFF4CAF50)

@Composable
fun ManHinhXacThucOTP(boDieuHuong: NavController, verificationId: String) {
    var maOTP by remember { mutableStateOf("") }
    var thoiGianDemNguoc by remember { mutableStateOf(59) }
    var dangXuLy by remember { mutableStateOf(false) }
    var thongBaoLoi by remember { mutableStateOf("") }

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    // Logic đếm ngược thời gian
    LaunchedEffect(Unit) {
        while (thoiGianDemNguoc > 0) {
            delay(1000)
            thoiGianDemNguoc--
        }
    }

    fun xacNhanMaOTP() {
        thongBaoLoi = ""
        if (maOTP.length != 6) {
            thongBaoLoi = "Mã OTP phải đủ 6 số"
            return
        }
        dangXuLy = true

        val credential = PhoneAuthProvider.getCredential(verificationId, maOTP)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { tacVu ->
                dangXuLy = false
                if (tacVu.isSuccessful) {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                    // --- ĐIỀU HƯỚNG VỀ HOME (Đúng chuẩn Route mới) ---
                    boDieuHuong.navigate(NavRoute.Home) {
                        // Xóa hết các màn hình đăng nhập trước đó khỏi stack
                        popUpTo(NavRoute.Login) { inclusive = true }
                    }
                } else {
                    val ngoaiLe = tacVu.exception
                    thongBaoLoi = if (ngoaiLe is FirebaseAuthInvalidCredentialsException) {
                        "Mã OTP không đúng. Vui lòng kiểm tra lại."
                    } else {
                        "Lỗi: ${ngoaiLe?.message}"
                    }
                }
            }
    }

    NenHinhSong {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Xác thực OTP",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MauXanhOTP, // Dùng màu mới
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Giao diện nhập OTP 6 số
            BasicTextField(
                value = maOTP,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        maOTP = it
                        thongBaoLoi = ""
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val char = if (index < maOTP.length) maOTP[index].toString() else ""

                            if (index == 3) {
                                Text(
                                    text = "-",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(45.dp)
                                    .height(55.dp)
                                    .padding(4.dp)
                                    .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        // Viền đỏ nếu có lỗi, ngược lại viền xanh hoặc trong suốt
                                        color = if (thongBaoLoi.isNotEmpty()) Color.Red else if (index < maOTP.length) MauXanhOTP else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dòng gửi lại mã
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Gửi lại mã ", color = MauXanhOTP, fontSize = 14.sp)
                Text(
                    text = "(${String.format("00:%02d", thoiGianDemNguoc)})",
                    color = MauXanhOTP,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (thongBaoLoi.isNotEmpty()) {
                Text(
                    text = thongBaoLoi,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = { xacNhanMaOTP() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauXanhOTP),
                shape = RoundedCornerShape(25.dp),
                enabled = !dangXuLy
            ) {
                if (dangXuLy) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { boDieuHuong.popBackStack() }) {
                Text("Quay lại", color = Color.Gray)
            }
        }
    }
}