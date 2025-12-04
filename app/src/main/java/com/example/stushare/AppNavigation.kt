package com.example.stushare

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // Quan trọng
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.google.firebase.auth.FirebaseAuth

// Import NavRoute
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.core.data.models.NotificationEntity

// Import Utils
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Import Screens
import com.example.stushare.features.auth.ui.*
import com.example.stushare.features.feature_home.ui.home.HomeScreen
import com.example.stushare.features.feature_home.ui.viewall.ViewAllScreen
import com.example.stushare.features.feature_search.ui.search.SearchScreen
import com.example.stushare.feature_search.ui.search.SearchResultScreen
import com.example.stushare.feature_document_detail.ui.detail.DocumentDetailScreen
import com.example.stushare.feature_request.ui.list.RequestListScreen
import com.example.stushare.features.feature_upload.ui.UploadScreen
import com.example.stushare.features.feature_upload.ui.UploadViewModel
import com.example.stushare.features.feature_request.ui.create.CreateRequestScreen
import com.example.stushare.features.feature_leaderboard.ui.LeaderboardScreen
import com.example.stushare.features.feature_leaderboard.ui.LeaderboardViewModel
import com.example.stushare.features.feature_notification.ui.NotificationScreen
import com.example.stushare.features.feature_document_detail.ui.pdf.PdfViewerScreen
import com.example.stushare.features.feature_profile.ui.main.ProfileScreen
import com.example.stushare.features.feature_profile.ui.main.ProfileViewModel
import com.example.stushare.features.feature_profile.ui.settings.SettingsScreen
import com.example.stushare.features.feature_profile.ui.account.AccountSecurityScreen
import com.example.stushare.features.feature_profile.ui.account.PersonalInfoScreen
import com.example.stushare.features.feature_profile.ui.account.ChangePasswordScreen
import com.example.stushare.features.feature_profile.ui.account.SwitchAccountScreen
import com.example.stushare.features.feature_profile.ui.account.EditAttributeScreen
import com.example.stushare.features.feature_profile.ui.settings.notification.NotificationSettingsScreen
import com.example.stushare.features.feature_profile.ui.settings.appearance.AppearanceSettingsScreen
import com.example.stushare.features.feature_profile.ui.settings.appearance.AppearanceViewModel
import com.example.stushare.features.feature_profile.ui.legal.AboutAppScreen
import com.example.stushare.features.feature_profile.ui.legal.ContactSupportScreen
import com.example.stushare.features.feature_profile.ui.legal.ReportViolationScreen
import com.example.stushare.features.feature_profile.ui.legal.TermsOfUseScreen
import com.example.stushare.features.feature_profile.ui.legal.PrivacyPolicyScreen
import com.example.stushare.feature_request.ui.detail.RequestDetailScreen

// Admin Imports
import com.example.stushare.features.feature_admin.ui.AdminScreen
import com.example.stushare.features.feature_admin.ui.AdminReportScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    // --- CẤU HÌNH ANIMATION ---
    val duration = 300
    val enterTransition = slideInHorizontally(animationSpec = tween(duration), initialOffsetX = { it }) + fadeIn(animationSpec = tween(duration))
    val exitTransition = slideOutHorizontally(animationSpec = tween(duration), targetOffsetX = { -it }) + fadeOut(animationSpec = tween(duration))
    val popEnterTransition = slideInHorizontally(animationSpec = tween(duration), initialOffsetX = { -it }) + fadeIn(animationSpec = tween(duration))
    val popExitTransition = slideOutHorizontally(animationSpec = tween(duration), targetOffsetX = { it }) + fadeOut(animationSpec = tween(duration))

    NavHost(
        navController = navController,
        startDestination = NavRoute.Intro,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(duration)) },
        exitTransition = { fadeOut(animationSpec = tween(duration)) },
        popEnterTransition = { fadeIn(animationSpec = tween(duration)) },
        popExitTransition = { fadeOut(animationSpec = tween(duration)) }
    ) {
        // ==========================================
        // 1. AUTHENTICATION
        // ==========================================
        composable<NavRoute.Intro> { ManHinhChao(navController) }
        composable<NavRoute.Onboarding> { ManHinhGioiThieu(navController) }
        
        composable<NavRoute.Login> { backStackEntry ->
            val args = backStackEntry.toRoute<NavRoute.Login>()
            ManHinhDangNhap(
                boDieuHuong = navController,
                emailMacDinh = args.email
            )
        }
        
        composable<NavRoute.Register> { ManHinhDangKy(navController) }
        composable<NavRoute.ForgotPassword> { ManHinhQuenMatKhau(navController) }
        composable<NavRoute.LoginSMS> { ManHinhDangNhapSDT(navController) }
        composable<NavRoute.VerifyOTP> { backStackEntry ->
            val args = backStackEntry.toRoute<NavRoute.VerifyOTP>()
            ManHinhXacThucOTP(navController, args.verificationId)
        }

        // ==========================================
        // 2. MAIN FEATURES
        // ==========================================
        composable<NavRoute.Home> {
            val context = LocalContext.current
            val msgLoginRequired = stringResource(R.string.msg_login_required)
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onSearchClick = { navController.navigate(NavRoute.Search) },
                onViewAllClick = { category -> navController.navigate(NavRoute.ViewAll(category)) },
                onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId)) },
                onCreateRequestClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.CreateRequest)
                    else {
                        Toast.makeText(context, msgLoginRequired, Toast.LENGTH_SHORT).show()
                        navController.navigate(NavRoute.Login())
                    }
                },
                onUploadClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        navController.navigate(NavRoute.Upload)
                    } else {
                        Toast.makeText(context, msgLoginRequired, Toast.LENGTH_SHORT).show()
                        navController.navigate(NavRoute.Login())
                    }
                },
                onLeaderboardClick = { navController.navigate(NavRoute.Leaderboard) },
                onNotificationClick = { navController.navigate(NavRoute.Notification) },
                onRequestListClick = { navController.navigate(NavRoute.RequestList) }
            )
        }

        composable<NavRoute.Search>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onSearchSubmit = { query -> navController.navigate(NavRoute.SearchResult(query)) }
            )
        }

        composable<NavRoute.SearchResult>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            SearchResultScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId.toString())) },
                onRequestClick = { navController.navigate(NavRoute.RequestList) }
            )
        }

        composable<NavRoute.DocumentDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.DocumentDetail>()
            val context = LocalContext.current
            val msgLoginRequired = stringResource(R.string.msg_login_required)

            DocumentDetailScreen(
                documentId = route.documentId,
                onBackClick = { navController.popBackStack() },
                onLoginRequired = {
                    Toast.makeText(context, msgLoginRequired, Toast.LENGTH_SHORT).show()
                    navController.navigate(NavRoute.Login())
                },
                onReadPdf = { url, title ->
                    if (url.isNotBlank()) {
                        try {
                            val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                            navController.navigate(NavRoute.PdfViewer(url = encodedUrl, title = title))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Lỗi đường dẫn file", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "File không tồn tại", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        composable<NavRoute.PdfViewer> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.PdfViewer>()
            PdfViewerScreen(
                url = route.url,
                title = route.title,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<NavRoute.ViewAll> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.ViewAll>()
            ViewAllScreen(
                category = route.category,
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId)) }
            )
        }

        composable<NavRoute.RequestList> {
            val context = LocalContext.current
            val msgLoginRequired = stringResource(R.string.msg_login_required)
            RequestListScreen(
                onBackClick = { navController.popBackStack() },
                onCreateRequestClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.CreateRequest)
                    else {
                        Toast.makeText(context, msgLoginRequired, Toast.LENGTH_SHORT).show()
                        navController.navigate(NavRoute.Login())
                    }
                },
                onNavigateToDetail = { requestId -> navController.navigate(NavRoute.RequestDetail(requestId)) }
            )
        }

        composable<NavRoute.RequestDetail> {
            RequestDetailScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.CreateRequest> {
            CreateRequestScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { navController.popBackStack() }
            )
        }

        composable<NavRoute.Upload>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val viewModel = hiltViewModel<UploadViewModel>()
            UploadScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.Leaderboard> {
            val viewModel = hiltViewModel<LeaderboardViewModel>()
            LeaderboardScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.Notification> {
            val context = LocalContext.current
            NotificationScreen(
                onBackClick = { navController.popBackStack() },
                onNotificationClick = { notification ->
                    when (notification.type) {
                        NotificationEntity.TYPE_UPLOAD,
                        NotificationEntity.TYPE_DOWNLOAD,
                        NotificationEntity.TYPE_RATING,
                        NotificationEntity.TYPE_COMMENT    -> {
                            if (notification.relatedId != null) {
                                navController.navigate(NavRoute.DocumentDetail(notification.relatedId))
                            } else {
                                Toast.makeText(context, "Không tìm thấy tài liệu liên kết", Toast.LENGTH_SHORT).show()
                            }
                        }
                        NotificationEntity.TYPE_SYSTEM -> { }
                    }
                }
            )
        }

        // ==========================================
        // 3. PROFILE & SETTINGS
        // ==========================================
        composable<NavRoute.Profile> {
            val viewModel = hiltViewModel<ProfileViewModel>()
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(NavRoute.Settings) },
                onNavigateToLeaderboard = { navController.navigate(NavRoute.Leaderboard) },
                onNavigateToLogin = { navController.navigate(NavRoute.Login()) },
                onNavigateToRegister = { navController.navigate(NavRoute.Register) },
                onDocumentClick = { docId -> navController.navigate(NavRoute.DocumentDetail(docId)) },
                onNavigateToUpload = { navController.navigate(NavRoute.Upload) },
                onNavigateToHome = { navController.navigate(NavRoute.Home) },
                onNavigateToAdmin = { navController.navigate(NavRoute.AdminDashboard) }
            )
        }

        // ==========================================
        // 4. ADMIN FEATURES
        // ==========================================

        composable<NavRoute.AdminDashboard>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            AdminScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToReports = { navController.navigate(NavRoute.AdminReports) }
            )
        }

        composable<NavRoute.AdminReports>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            AdminReportScreen(
                onBackClick = { navController.popBackStack() },
                onDocumentClick = { documentId ->
                    navController.navigate(NavRoute.DocumentDetail(documentId))
                }
            )
        }

        composable<NavRoute.Settings>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            val msgLogoutSuccess = stringResource(R.string.msg_logout_success)
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onAccountSecurityClick = { navController.navigate(NavRoute.AccountSecurity) },
                onNotificationSettingsClick = { navController.navigate(NavRoute.NotificationSettings) },
                onAppearanceSettingsClick = { navController.navigate(NavRoute.AppearanceSettings) },
                onAboutAppClick = { navController.navigate(NavRoute.AboutApp) },
                onContactSupportClick = { navController.navigate(NavRoute.ContactSupport) },
                onReportViolationClick = { navController.navigate(NavRoute.ReportViolation) },
                onSwitchAccountClick = { navController.navigate(NavRoute.SwitchAccount) },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, msgLogoutSuccess, Toast.LENGTH_SHORT).show()
                    navController.navigate(NavRoute.Login()) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 🟢 MÀN HÌNH TÀI KHOẢN & BẢO MẬT
        composable<NavRoute.AccountSecurity>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            val viewModel = hiltViewModel<ProfileViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val user = FirebaseAuth.getInstance().currentUser
            val currentEmail = user?.email ?: ""
            val currentPhone = user?.phoneNumber ?: ""

            AccountSecurityScreen(
                userEmail = currentEmail,
                userPhone = currentPhone,
                onBackClick = { navController.popBackStack() },
                onPersonalInfoClick = { navController.navigate(NavRoute.PersonalInfo) },
                onPhoneClick = { navController.navigate(NavRoute.EditPhone) },
                onEmailClick = { }, 
                onPasswordClick = { navController.navigate(NavRoute.ChangePassword) },
                onDeleteAccountClick = { Toast.makeText(context, "Chức năng cần xác thực lại", Toast.LENGTH_SHORT).show() }
            )
        }

        // 🟢 ROUTE MỚI: CHỈNH SỬA EMAIL (Đã Việt hóa)
        composable<NavRoute.EditEmail>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            val viewModel = hiltViewModel<ProfileViewModel>()
            val user = FirebaseAuth.getInstance().currentUser
            
            var showPasswordDialog by remember { mutableStateOf(false) }
            var pendingNewEmail by remember { mutableStateOf("") }
            val errEmailSame = stringResource(R.string.err_email_same)

            LaunchedEffect(Unit) {
                viewModel.updateMessage.collect { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (msg.contains("thành công", ignoreCase = true)) {
                        navController.popBackStack() 
                    }
                }
            }

            EditAttributeScreen(
                title = stringResource(R.string.title_edit_email), // "Cập nhật Email"
                initialValue = user?.email ?: "",
                label = stringResource(R.string.label_edit_email), // "Địa chỉ Email"
                onBackClick = { navController.popBackStack() },
                onSaveClick = { newEmail ->
                    if (newEmail == user?.email) {
                        Toast.makeText(context, errEmailSame, Toast.LENGTH_SHORT).show()
                    } else {
                        pendingNewEmail = newEmail
                        showPasswordDialog = true
                    }
                },
                keyboardType = KeyboardType.Email
            )

            if (showPasswordDialog) {
                ReAuthenticateDialog(
                    onDismiss = { showPasswordDialog = false },
                    onConfirm = { password ->
                        showPasswordDialog = false
                        viewModel.updateEmail(currentPass = password, newEmail = pendingNewEmail)
                    }
                )
            }
        }

        // 🟢 ROUTE MỚI: CHỈNH SỬA SỐ ĐIỆN THOẠI (Đã Việt hóa & Hỗ trợ tiếng Anh)
        composable<NavRoute.EditPhone>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            val activity = LocalContext.current as? android.app.Activity
            val user = FirebaseAuth.getInstance().currentUser
            val viewModel = hiltViewModel<ProfileViewModel>()

            var showOtpDialog by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(false) }

            // Các chuỗi thông báo
            val msgSendingOtp = stringResource(R.string.msg_sending_otp) // "Đang gửi OTP đến %1$s"
            val msgOtpSent = stringResource(R.string.msg_otp_sent)
            val errPhoneEmpty = stringResource(R.string.err_phone_empty)
            val errGeneric = stringResource(R.string.err_generic)

            LaunchedEffect(Unit) {
                viewModel.updateMessage.collect { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (msg.contains("thành công", ignoreCase = true)) {
                        showOtpDialog = false
                        navController.popBackStack()
                    }
                }
            }

            EditAttributeScreen(
                title = stringResource(R.string.title_edit_phone), // "Cập nhật SĐT"
                initialValue = user?.phoneNumber ?: "",
                label = stringResource(R.string.hint_phone_input), // "Nhập số điện thoại..."
                onBackClick = { navController.popBackStack() },
                onSaveClick = { rawPhone ->
                    if (activity != null && rawPhone.isNotBlank()) {
                        var formattedPhone = rawPhone.trim()
                        if (formattedPhone.startsWith("0")) {
                            formattedPhone = "+84" + formattedPhone.substring(1)
                        } else if (!formattedPhone.startsWith("+")) {
                            formattedPhone = "+84$formattedPhone"
                        }

                        isLoading = true
                        // Sử dụng String.format để chèn số điện thoại vào chuỗi resource
                        Toast.makeText(context, String.format(msgSendingOtp, formattedPhone), Toast.LENGTH_SHORT).show()
                        
                        viewModel.sendOtp(
                            phoneNumber = formattedPhone,
                            activity = activity,
                            onCodeSent = {
                                isLoading = false
                                showOtpDialog = true
                                Toast.makeText(context, msgOtpSent, Toast.LENGTH_SHORT).show()
                            },
                            onError = { errorMsg ->
                                isLoading = false
                                Toast.makeText(context, String.format(errGeneric, errorMsg), Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, errPhoneEmpty, Toast.LENGTH_SHORT).show()
                    }
                },
                keyboardType = KeyboardType.Phone // Bàn phím số
            )

            // Dialog OTP
            if (showOtpDialog) {
                var otpCode by remember { mutableStateOf("") }
                val errOtpLength = stringResource(R.string.err_otp_length)
                
                AlertDialog(
                    onDismissRequest = { showOtpDialog = false },
                    title = { Text(stringResource(R.string.title_enter_otp)) },
                    text = {
                        Column {
                            Text(stringResource(R.string.desc_otp_sent))
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text(stringResource(R.string.label_otp_input)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { 
                            if (otpCode.length == 6) {
                                viewModel.verifyAndUpdatePhone(otpCode)
                            } else {
                                Toast.makeText(context, errOtpLength, Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text(stringResource(R.string.btn_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showOtpDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }

        composable<NavRoute.PersonalInfo>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            PersonalInfoScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.ChangePassword>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            ChangePasswordScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.SwitchAccount>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            SwitchAccountScreen(
                onBackClick = { navController.popBackStack() },
                onAddAccountClick = { emailCanDangNhap ->
                    FirebaseAuth.getInstance().signOut()
                    
                    navController.navigate(NavRoute.Login(email = emailCanDangNhap)) {
                        popUpTo(0) { inclusive = true } 
                        launchSingleTop = true
                    }
                    
                    if (emailCanDangNhap != null) {
                        Toast.makeText(context, "Vui lòng nhập mật khẩu cho $emailCanDangNhap", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Vui lòng đăng nhập tài khoản mới", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        composable<NavRoute.NotificationSettings>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            NotificationSettingsScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.AppearanceSettings>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val viewModel = hiltViewModel<AppearanceViewModel>()
            AppearanceSettingsScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.AboutApp>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            AboutAppScreen(
                onBackClick = { navController.popBackStack() },
                onTermsClick = { navController.navigate(NavRoute.TermsOfUse) },     
                onPrivacyClick = { navController.navigate(NavRoute.PrivacyPolicy) } 
            )
        }

        composable<NavRoute.ContactSupport>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            ContactSupportScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.ReportViolation>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            ReportViolationScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.TermsOfUse> {
            TermsOfUseScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.PrivacyPolicy> {
            PrivacyPolicyScreen(onBackClick = { navController.popBackStack() })
        }
    } // Kết thúc NavHost
}

@Composable
fun ReAuthenticateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.title_security_auth)) },
        text = {
            Column {
                Text(stringResource(R.string.desc_enter_password))
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.acc_sec_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}