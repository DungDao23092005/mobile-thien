package com.example.stushare

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stushare.core.data.repository.SettingsRepository
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.features.feature_home.ui.components.BottomNavBar
import com.example.stushare.ui.theme.StuShareTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            // 1. Dark Theme
            val isDarkTheme by settingsRepository.isDarkTheme
                .collectAsState(initial = isSystemInDarkTheme())

            // 2. Font Scale
            val fontScale by settingsRepository.fontScale
                .collectAsState(initial = 1.0f)

            // 3. Language - QUAN TRỌNG: Để initial = null để đợi DataStore load xong
            val languageCode by settingsRepository.languageCode
                .collectAsState(initial = null)

            // Xử lý thay đổi ngôn ngữ
            LaunchedEffect(languageCode) {
                // Chỉ chạy khi languageCode đã có dữ liệu (không null)
                languageCode?.let { code ->
                    val currentLocales = AppCompatDelegate.getApplicationLocales()
                    // So sánh ngôn ngữ hiện tại và ngôn ngữ trong cài đặt
                    if (currentLocales.toLanguageTags() != code) {
                        val newLocale = LocaleListCompat.forLanguageTags(code)
                        AppCompatDelegate.setApplicationLocales(newLocale)
                    }
                }
            }

            StuShareTheme(
                darkTheme = isDarkTheme,
                fontScale = fontScale
            ) {
                MainAppScreen(windowSizeClass = windowSizeClass)
            }
        }
    }
}

@Composable
fun MainAppScreen(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Kết nối ViewModel để lấy dữ liệu Badge (số lượng thông báo chưa đọc)
    val mainViewModel: MainViewModel = hiltViewModel()
    val unreadCount by mainViewModel.unreadCount.collectAsState(initial = 0)

    // Các màn hình cần hiển thị BottomNavigationBar
    val showBottomBar = listOf(
        NavRoute.Home,
        NavRoute.Search,
        NavRoute.Notification,
        NavRoute.Profile,
        NavRoute.RequestList
    ).any { route ->
        currentDestination?.hasRoute(route::class) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                // Đảm bảo BottomBar không bị ảnh hưởng bởi FontScale người dùng chọn
                val currentDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density = currentDensity.density, fontScale = 1.0f)
                ) {
                    BottomNavBar(
                        navController = navController,
                        unreadNotificationCount = unreadCount
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavigation(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
    }
}