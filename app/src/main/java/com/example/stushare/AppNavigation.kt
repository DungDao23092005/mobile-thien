package com.example.stushare

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
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
        // 4. ADMIN FEATURES (Đã cập nhật hoàn chỉnh)
        // ==========================================

        // 🟢 Màn hình Dashboard (Menu Admin)
        composable<NavRoute.AdminDashboard>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            AdminScreen(
                onBackClick = { navController.popBackStack() },
                // 👇 Sự kiện chuyển sang màn hình danh sách báo cáo
                onNavigateToReports = { navController.navigate(NavRoute.AdminReports) }
            )
        }

        // 🟢 Màn hình Danh sách Báo cáo Vi phạm
        composable<NavRoute.AdminReports>(
            enterTransition = { enterTransition }, exitTransition = { exitTransition },
            popEnterTransition = { popEnterTransition }, popExitTransition = { popExitTransition }
        ) {
            AdminReportScreen(
                onBackClick = { navController.popBackStack() },
                // 👇 Sự kiện bấm vào một báo cáo để xem tài liệu gốc
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
            AccountSecurityScreen(
                onBackClick = { navController.popBackStack() },
                onPersonalInfoClick = { navController.navigate(NavRoute.PersonalInfo) },
                onPhoneClick = { Toast.makeText(context, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show() },
                onEmailClick = { Toast.makeText(context, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show() },
                onPasswordClick = { navController.navigate(NavRoute.ChangePassword) },
                onDeleteAccountClick = { Toast.makeText(context, "Chức năng cần xác thực lại", Toast.LENGTH_SHORT).show() }
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