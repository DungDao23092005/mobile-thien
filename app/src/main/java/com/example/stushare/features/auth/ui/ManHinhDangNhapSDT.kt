package com.example.stushare.features.auth.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
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
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

// --- ĐỊNH NGHĨA MÀU RIÊNG ---
val MauXanhSDT = Color(0xFF4CAF50)

@Composable
fun ManHinhDangNhapSDT(boDieuHuong: NavController) {
    var soDienThoai by remember { mutableStateOf("") }
    var loiSdt by remember { mutableStateOf(false) }
    var thongBaoLoi by remember { mutableStateOf("") }
    var dangXuLy by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()

    fun kiemTraDinhDangSDT(sdt: String): Boolean {
        val pattern = Regex("^0[0-9]{9}$")
        return pattern.matches(sdt)
    }

    fun guiMaOTP() {
        loiSdt = false
        thongBaoLoi = ""

        if (soDienThoai.isEmpty()) {
            thongBaoLoi = "Vui lòng nhập số điện thoại"
            return
        }
        if (!kiemTraDinhDangSDT(soDienThoai)) {
            loiSdt = true
            return
        }

        dangXuLy = true
        // Chuyển đổi số điện thoại về định dạng quốc tế +84
        val soDienThoaiChuan = "+84${soDienThoai.substring(1)}"

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                dangXuLy = false
            }

            override fun onVerificationFailed(e: FirebaseException) {
                dangXuLy = false
                val thongBaoGoc = e.message ?: ""
                thongBaoLoi = when {
                    e is FirebaseAuthInvalidCredentialsException -> "Số điện thoại không hợp lệ."
                    e is FirebaseTooManyRequestsException -> "Quá nhiều yêu cầu. Vui lòng đợi lát sau."
                    thongBaoGoc.contains("BILLING_NOT_ENABLED") -> "Vui lòng bật Phone Auth trên Firebase Console."
                    else -> "Lỗi: $thongBaoGoc"
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                dangXuLy = false
                Toast.makeText(context, "Đã gửi mã OTP", Toast.LENGTH_SHORT).show()

                // --- SỬA NAVIGATE: Dùng NavRoute.VerifyOTP ---
                boDieuHuong.navigate(NavRoute.VerifyOTP(verificationId))
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(soDienThoaiChuan)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
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
                text = "Đăng Nhập",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MauXanhSDT // Dùng màu mới định nghĩa
            )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = soDienThoai,
                onValueChange = {
                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                        soDienThoai = it
                        loiSdt = false
                        thongBaoLoi = ""
                    }
                },
                placeholder = { Text("Số điện thoại (10 số)", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (loiSdt) Color.Red else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(Color.Transparent),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color(0xFFE0E0E0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = loiSdt
            )

            if (loiSdt) {
                Text(
                    text = "Số điện thoại không đúng định dạng (cần 10 số, bắt đầu là 0)",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp, top = 4.dp)
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
                onClick = { guiMaOTP() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauXanhSDT),
                shape = RoundedCornerShape(25.dp),
                enabled = !dangXuLy
            ) {
                if (dangXuLy) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Gửi mã OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { boDieuHuong.popBackStack() }) {
                Text("Quay lại", color = Color.Gray)
            }
        }
    }
}