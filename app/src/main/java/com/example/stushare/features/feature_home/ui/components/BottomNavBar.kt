package com.example.stushare.features.feature_home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.stushare.R
import com.example.stushare.core.navigation.NavRoute
import com.example.stushare.ui.theme.PrimaryGreen

@Composable
fun BottomNavBar(
    navController: NavController,
    unreadNotificationCount: Int = 0
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Tối ưu: Chỉ tạo lại danh sách khi số lượng thông báo thay đổi
    val leftItems = remember {
        listOf(
            NavigationItem(title = "Trang chủ", selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home, route = NavRoute.Home),
            NavigationItem(title = "Tìm kiếm", selectedIcon = Icons.Filled.Search, unselectedIcon = Icons.Outlined.Search, route = NavRoute.Search)
        )
    }

    val rightItems = remember(unreadNotificationCount) {
        listOf(
            NavigationItem(
                title = "Thông báo",
                selectedIcon = Icons.Filled.Notifications,
                unselectedIcon = Icons.Outlined.Notifications,
                route = NavRoute.Notification,
                badgeCount = unreadNotificationCount
            ),
            NavigationItem(title = "Cá nhân", selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person, route = NavRoute.Profile)
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Nền thanh Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = PrimaryGreen,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trái
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    leftItems.forEach { item ->
                        BottomNavItem(item, currentDestination?.hasRoute(item.route::class) == true) {
                            navigateSafe(navController, item.route)
                        }
                    }
                }
                // Giữa (Khoảng trống cho nút Upload)
                Spacer(modifier = Modifier.size(60.dp))
                // Phải
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    rightItems.forEach { item ->
                        BottomNavItem(item, currentDestination?.hasRoute(item.route::class) == true) {
                            navigateSafe(navController, item.route)
                        }
                    }
                }
            }
        }

        // 2. Nút Upload (Nổi ở giữa)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 10.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { navigateSafe(navController, NavRoute.Upload) }
                .shadow(8.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).background(PrimaryGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.upload_header),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavItem(item: NavigationItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(8.dp)
    ) {
        BadgedBox(
            badge = {
                if (item.badgeCount > 0) {
                    Badge(
                        containerColor = Color.Red, // Màu đỏ nổi bật
                        contentColor = Color.White,
                        modifier = Modifier
                            // Điều chỉnh offset để chấm nằm góc trên bên phải đẹp hơn
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(8.dp)
                    )
                }
            }
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.title,
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(26.dp)
            )
        }

        if (isSelected) {
            Text(text = item.title, fontSize = 10.sp, color = Color.White)
        }
    }
}

fun navigateSafe(navController: NavController, route: NavRoute) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: NavRoute,
    val badgeCount: Int = 0
)