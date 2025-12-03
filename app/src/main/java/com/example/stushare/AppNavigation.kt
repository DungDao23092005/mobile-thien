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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp // 🟢 ĐÃ THÊM IMPORT NÀY
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
import com.example.stushare.feature_request.ui.detail.RequestDetailScreen

// 🟢 ADMIN IMPORTS
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
        composable<NavRoute.Login> { ManHinhDangNhap(navController) }
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
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onSearchClick = { navController.navigate(NavRoute.Search) },
                onViewAllClick = { category -> navController.navigate(NavRoute.ViewAll(category)) },
                onDocumentClick = { documentId -> navController.navigate(NavRoute.DocumentDetail(documentId)) },
                onCreateRequestClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.CreateRequest)
                    else {
                        Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show()
                        navController.navigate(NavRoute.Login)
                    }
                },
                onUploadClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        navController.navigate(NavRoute.Upload)
                    } else {
                        Toast.makeText(context, "Bạn cần đăng nhập để đăng tài liệu!", Toast.LENGTH_SHORT).show()
                        navController.navigate(NavRoute.Login)
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

            DocumentDetailScreen(
                documentId = route.documentId,
                onBackClick = { navController.popBackStack() },
                onLoginRequired = {
                    Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show()
                    navController.navigate(NavRoute.Login)
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
            RequestListScreen(
                onBackClick = { navController.popBackStack() },
                onCreateRequestClick = {
                    if (FirebaseAuth.getInstance().currentUser != null) navController.navigate(NavRoute.CreateRequest)
                    else {
                        Toast.makeText(context, "Cần đăng nhập!", Toast.LENGTH_SHORT).show()
                        navController.navigate(NavRoute.Login)
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
                onNavigateToLogin = { navController.navigate(NavRoute.Login) },
                onNavigateToRegister = { navController.navigate(NavRoute.Register) },
                onDocumentClick = { docId -> navController.navigate(NavRoute.DocumentDetail(docId)) },
                onNavigateToUpload = { navController.navigate(NavRoute.Upload) },
                onNavigateToHome = { navController.navigate(NavRoute.Home) },

                // Điều hướng tới Admin Dashboard
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
                    Toast.makeText(context, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()
                    navController.navigate(NavRoute.Login) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<NavRoute.AccountSecurity>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            val viewModel = hiltViewModel<ProfileViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Lấy email và SĐT từ ViewModel (ProfileUiState)
            val user = FirebaseAuth.getInstance().currentUser
            val currentEmail = user?.email ?: ""
            val currentPhone = user?.phoneNumber ?: ""

            AccountSecurityScreen(
                userEmail = currentEmail,
                userPhone = currentPhone,
                onBackClick = { navController.popBackStack() },
                onPersonalInfoClick = { navController.navigate(NavRoute.PersonalInfo) },
                // 🟢 Chuyển sang màn hình EditPhone
                onPhoneClick = { navController.navigate(NavRoute.EditPhone) },
                // 🟢 Chuyển sang màn hình EditEmail
                onEmailClick = { navController.navigate(NavRoute.EditEmail) },
                onPasswordClick = { navController.navigate(NavRoute.ChangePassword) },
                onDeleteAccountClick = { Toast.makeText(context, "Chức năng cần xác thực lại", Toast.LENGTH_SHORT).show() }
            )
        }

        // 🟢 ROUTE MỚI: CHỈNH SỬA EMAIL
        composable<NavRoute.EditEmail>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            val viewModel = hiltViewModel<ProfileViewModel>()
            val user = FirebaseAuth.getInstance().currentUser
            
            // Các biến trạng thái để quản lý Dialog
            var showPasswordDialog by remember { mutableStateOf(false) }
            var pendingNewEmail by remember { mutableStateOf("") }

            // Lắng nghe kết quả từ ViewModel (để hiển thị Toast hoặc đóng màn hình)
            LaunchedEffect(Unit) {
                viewModel.updateMessage.collect { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (msg.contains("thành công", ignoreCase = true)) {
                        navController.popBackStack() // Quay về nếu thành công
                    }
                }
            }

            // Màn hình chính
            EditAttributeScreen(
                title = "Cập nhật Email",
                initialValue = user?.email ?: "",
                label = "Email mới",
                onBackClick = { navController.popBackStack() },
                onSaveClick = { newEmail ->
                    if (newEmail == user?.email) {
                        Toast.makeText(context, "Email mới trùng với email hiện tại", Toast.LENGTH_SHORT).show()
                    } else {
                        // Lưu email tạm và hiện Dialog nhập pass
                        pendingNewEmail = newEmail
                        showPasswordDialog = true
                    }
                }
            )

            // Hiển thị Dialog nếu cần
            if (showPasswordDialog) {
                ReAuthenticateDialog(
                    onDismiss = { showPasswordDialog = false },
                    onConfirm = { password ->
                        showPasswordDialog = false
                        // Gọi ViewModel để thực hiện đổi email
                        viewModel.updateEmail(currentPass = password, newEmail = pendingNewEmail)
                    }
                )
            }
        }

        // 🟢 ROUTE MỚI: CHỈNH SỬA SỐ ĐIỆN THOẠI
        composable<NavRoute.EditPhone>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            val context = LocalContext.current
            val user = FirebaseAuth.getInstance().currentUser

            EditAttributeScreen(
                title = "Cập nhật SĐT",
                initialValue = user?.phoneNumber ?: "",
                label = "Số điện thoại mới (+84...)",
                onBackClick = { navController.popBackStack() },
                onSaveClick = { newPhone ->
                    Toast.makeText(context, "Đang gửi mã OTP đến $newPhone...", Toast.LENGTH_SHORT).show()
                    // Logic thực tế cần quy trình Verify OTP của Firebase Phone Auth
                    navController.popBackStack()
                }
            )
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
            SwitchAccountScreen(onBackClick = { navController.popBackStack() })
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
            val context = LocalContext.current
            AboutAppScreen(
                onBackClick = { navController.popBackStack() },
                onPrivacyClick = { Toast.makeText(context, "Đang mở chính sách...", Toast.LENGTH_SHORT).show() },
                onTermsClick = { Toast.makeText(context, "Đang mở điều khoản...", Toast.LENGTH_SHORT).show() }
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
    }
}

// 🟢 COMPONENT: HỘP THOẠI XÁC THỰC MẬT KHẨU
@Composable
fun ReAuthenticateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Xác thực bảo mật") },
        text = {
            Column {
                Text("Vui lòng nhập mật khẩu hiện tại để tiếp tục:")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), // Ẩn mật khẩu
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank()
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}