package com.example.stushare.features.feature_profile.ui.settings.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // 1. Lấy trạng thái Dark Mode
    val isDarkTheme: StateFlow<Boolean> = settingsRepository.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // 2. Lấy Ngôn ngữ hiện tại
    val language: StateFlow<String> = settingsRepository.languageCode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "vi"
        )

    // 3. Lấy Cỡ chữ
    val fontScale: StateFlow<Float> = settingsRepository.fontScale
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1.0f
        )

    // --- CÁC HÀM CẬP NHẬT ---

    // Bật/Tắt Dark Mode (Có hiệu lực ngay lập tức)
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(isDark)
        }
    }

    // Đổi Cỡ chữ (Có hiệu lực ngay lập tức)
    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            settingsRepository.setFontScale(scale)
        }
    }

    // ⭐️ QUAN TRỌNG: Đổi Ngôn ngữ và yêu cầu Restart
    // Hàm này nhận vào một hành động (onRestart) để UI gọi lệnh khởi động lại app
    fun setLanguageAndRestart(lang: String, onRestart: () -> Unit) {
        viewModelScope.launch {
            // 1. Lưu ngôn ngữ mới vào DataStore
            settingsRepository.setLanguage(lang)

            // 2. Sau khi lưu xong, gọi callback để UI thực hiện Restart
            onRestart()
        }
    }
}