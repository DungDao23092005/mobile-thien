package com.example.stushare.features.auth.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stushare.R
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.ui.theme.PrimaryGreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ManHinhDangNhap(
    boDieuHuong: NavController,
    emailMacDinh: String? = null
) {
    var email by remember { mutableStateOf(emailMacDinh ?: "") }
    var matKhau by remember { mutableStateOf("") }
    var hienThiMatKhau by remember { mutableStateOf(false) }
    var dangXuLy by remember { mutableStateOf(false) }
    var thongBaoLoi by remember { mutableStateOf("") }

    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // 🟢 HÀM CHUNG: Kiểm tra trạng thái tài khoản sau khi Auth thành công
    // (Dùng cho cả Email và Google)
    fun checkLoginStatus(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                dangXuLy = false
                // Kiểm tra xem tài khoản có bị khóa không
                val isBanned = document.getBoolean("banned") ?: false

                if (isBanned) {
                    firebaseAuth.signOut()
                    thongBaoLoi = "Tài khoản của bạn đã bị khóa do vi phạm chính sách."
                } else {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    boDieuHuong.navigate(NavRoute.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            .addOnFailureListener { e ->
                dangXuLy = false
                firebaseAuth.signOut()
                thongBaoLoi = "Lỗi kiểm tra thông tin: ${e.message}"
            }
    }

    // 🟢 XỬ LÝ: Đăng nhập bằng Email/Pass
    fun thucHienDangNhapEmail() {
        thongBaoLoi = ""
        if (email.isEmpty() || matKhau.isEmpty()) {
            thongBaoLoi = "Vui lòng nhập đầy đủ thông tin"
            return
        }

        dangXuLy = true
        firebaseAuth.signInWithEmailAndPassword(email, matKhau)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        checkLoginStatus(userId)
                    } else {
                        dangXuLy = false
                        thongBaoLoi = "Lỗi xác thực người dùng."
                    }
                } else {
                    dangXuLy = false
                    val ngoaiLe = task.exception
                    thongBaoLoi = when (ngoaiLe) {
                        is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại."
                        is FirebaseAuthInvalidCredentialsException -> "Sai email hoặc mật khẩu."
                        else -> "Lỗi: ${ngoaiLe?.message}"
                    }
                }
            }
    }

    // 🟢 CẤU HÌNH GOOGLE SIGN IN
    // Lấy default_web_client_id từ file google-services.json (Android Studio tự sinh ra)
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // 🟢 XỬ LÝ: Kết quả trả về từ Google Launcher
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Lấy tài khoản Google thành công
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                
                if (idToken != null) {
                    dangXuLy = true
                    // Dùng token này để đăng nhập vào Firebase
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val userId = firebaseAuth.currentUser?.uid
                                if (userId != null) {
                                    checkLoginStatus(userId)
                                }
                            } else {
                                dangXuLy = false
                                thongBaoLoi = "Lỗi xác thực Firebase: ${authTask.exception?.message}"
                            }
                        }
                } else {
                    dangXuLy = false
                    thongBaoLoi = "Không lấy được ID Token từ Google"
                }
            } catch (e: ApiException) {
                dangXuLy = false
                // Mã lỗi thường gặp: 10, 12500 (thường do SHA-1 sai)
                thongBaoLoi = "Đăng nhập Google thất bại (Mã lỗi: ${e.statusCode})"
            }
        } else {
            dangXuLy = false // Người dùng hủy đăng nhập (bấm nút Back/Hủy)
        }
    }

    // --- UI ---
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
                color = PrimaryGreen
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
                leadingIcon = { 
                    Icon(
                        Icons.Default.Email, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant 
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp, 
                        if (thongBaoLoi.isNotEmpty()) MaterialTheme.colorScheme.error else Color.Transparent, 
                        RoundedCornerShape(16.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                leadingIcon = { 
                    Icon(
                        Icons.Default.Lock, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant 
                    ) 
                },
                trailingIcon = {
                    IconButton(onClick = { hienThiMatKhau = !hienThiMatKhau }) {
                        Icon(
                            imageVector = if (hienThiMatKhau) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp, 
                        if (thongBaoLoi.isNotEmpty()) MaterialTheme.colorScheme.error else Color.Transparent, 
                        RoundedCornerShape(16.dp)
                    ),
                visualTransformation = if (hienThiMatKhau) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Hiển thị lỗi
            if (thongBaoLoi.isNotEmpty()) {
                Text(
                    text = thongBaoLoi,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quên mật khẩu
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "Quên mật khẩu?",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        boDieuHuong.navigate(NavRoute.ForgotPassword)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút Đăng nhập Email
            Button(
                onClick = { thucHienDangNhapEmail() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                ),
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

            // Text "Hoặc đăng nhập bằng"
            Text(
                "Hoặc đăng nhập bằng", 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Hàng nút Mạng xã hội
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 🟢 NÚT GOOGLE ĐÃ ĐƯỢC GẮN HÀM XỬ LÝ
                IconButton(
                    onClick = { 
                        if (!dangXuLy) {
                            dangXuLy = true
                            // Kích hoạt Intent đăng nhập Google
                            googleLauncher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Nút Đăng nhập SĐT
                IconButton(
                    onClick = { boDieuHuong.navigate(NavRoute.LoginSMS) },
                    modifier = Modifier
                        .size(50.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_dienthoai1),
                        contentDescription = "Phone",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Text(
                    "Chưa có tài khoản? ", 
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Đăng ký ngay",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        boDieuHuong.navigate(NavRoute.Register)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    boDieuHuong.navigate(NavRoute.Home) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Tiếp tục với vai trò Khách 👤",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}