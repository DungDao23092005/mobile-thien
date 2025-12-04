package com.example.stushare.features.feature_profile.ui.settings.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.R
// import com.example.stushare.core.utils.restartApp // <-- Không cần dùng nữa
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Lấy dữ liệu từ ViewModel
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val currentFontScale by viewModel.fontScale.collectAsStateWithLifecycle()

    // State hiển thị Dialog
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }

    // State Dialog xác nhận khởi động lại (đổi ngôn ngữ)
    var showRestartDialog by remember { mutableStateOf(false) }
    var pendingLanguage by remember { mutableStateOf("") }

    // Màu sắc từ Theme
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val dividerColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.appearance_language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // --- 1. Giao diện Sáng/Tối ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.appearance_dark_mode),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.toggleTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                HorizontalDivider(color = dividerColor)

                // --- 2. Đổi cỡ chữ ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .clickable { showFontDialog = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.appearance_font_size),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor,
                        modifier = Modifier.weight(1f)
                    )

                    val fontSizeLabel = when (currentFontScale) {
                        0.85f -> stringResource(R.string.font_small)
                        1.15f -> stringResource(R.string.font_large)
                        else -> stringResource(R.string.font_medium)
                    }

                    Text(
                        text = fontSizeLabel,
                        color = onSurfaceColor.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = onSurfaceColor.copy(alpha = 0.5f)
                    )
                }

                // --- 3. Header Ngôn ngữ ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.appearance_language_header),
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // --- Item Thay đổi ngôn ngữ ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .clickable { showLanguageDialog = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.appearance_change_language),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = onSurfaceColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    val currentLangLabel = if (currentLang == "vi") "Tiếng Việt" else "English"
                    Text(
                        text = currentLangLabel,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryGreen
                    )
                }
            }

            // --- Bottom Curve ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(100.dp)
                    .offset(y = 50.dp)
                    .background(
                        color = PrimaryGreen,
                        shape = RoundedCornerShape(topStart = 1000.dp, topEnd = 1000.dp)
                    )
            )
        }
    }

    // --- Dialog Chọn Ngôn Ngữ ---
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.appearance_change_language), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    LanguageOption("Tiếng Việt", selected = currentLang == "vi") {
                        if (currentLang != "vi") {
                            pendingLanguage = "vi"
                            showLanguageDialog = false
                            showRestartDialog = true
                        } else {
                            showLanguageDialog = false
                        }
                    }
                    LanguageOption("English", selected = currentLang == "en") {
                        if (currentLang != "en") {
                            pendingLanguage = "en"
                            showLanguageDialog = false
                            showRestartDialog = true
                        } else {
                            showLanguageDialog = false
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel), color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = surfaceColor,
            titleContentColor = onSurfaceColor,
            textContentColor = onSurfaceColor
        )
    }

    // ⭐️ DIALOG XÁC NHẬN (ĐÃ SỬA LOGIC)
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = PrimaryGreen) },
            title = { Text("Cần khởi động lại") },
            text = {
                Text("Để thay đổi ngôn ngữ hoàn tất, ứng dụng cần được tải lại. Bạn có muốn tiếp tục?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestartDialog = false
                        // CHỈ LƯU VÀO DATASTORE. KHÔNG GỌI RESTART APP THỦ CÔNG.
                        // MainActivity sẽ lắng nghe và tự recreate activity.
                        viewModel.setLanguageAndRestart(pendingLanguage) {
                            // Empty lambda: Không làm gì ở đây cả
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Đồng ý", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text(stringResource(R.string.cancel), color = onSurfaceColor)
                }
            },
            containerColor = surfaceColor,
            titleContentColor = onSurfaceColor,
            textContentColor = onSurfaceColor
        )
    }

    // --- Dialog Chọn Cỡ Chữ ---
    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text(stringResource(R.string.appearance_font_size), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    FontOption(stringResource(R.string.font_small), selected = currentFontScale == 0.85f) {
                        viewModel.setFontScale(0.85f)
                        showFontDialog = false
                    }
                    FontOption(stringResource(R.string.font_medium), selected = currentFontScale == 1.0f) {
                        viewModel.setFontScale(1.0f)
                        showFontDialog = false
                    }
                    FontOption(stringResource(R.string.font_large), selected = currentFontScale == 1.15f) {
                        viewModel.setFontScale(1.15f)
                        showFontDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontDialog = false }) {
                    Text(stringResource(R.string.cancel), color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = surfaceColor,
            titleContentColor = onSurfaceColor,
            textContentColor = onSurfaceColor
        )
    }
}

@Composable
fun LanguageOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
        )
        Text(
            text,
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FontOption(text: String, selected: Boolean, onClick: () -> Unit) {
    LanguageOption(text, selected, onClick)
}