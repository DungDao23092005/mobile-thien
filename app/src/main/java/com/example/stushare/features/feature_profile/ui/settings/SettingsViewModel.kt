package com.example.stushare.features.feature_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // 1. Lấy ngôn ngữ hiện tại để hiển thị dấu tích (✓)
    val currentLanguage = settingsRepository.languageCode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "vi"
        )

    // 2. Hàm thay đổi ngôn ngữ
    fun changeLanguage(code: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(code)
            // Lưu xong, DataStore sẽ báo về MainActivity -> MainActivity tự đổi ngôn ngữ
        }
    }
}