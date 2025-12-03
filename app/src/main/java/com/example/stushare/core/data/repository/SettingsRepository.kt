package com.example.stushare.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Định nghĩa DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Định nghĩa tất cả các Keys
    private object PreferencesKeys {
        // 1. User & Caching
        val USER_NAME = stringPreferencesKey("user_name")
        val LAST_REFRESH_TIMESTAMP = longPreferencesKey("last_refresh_timestamp")

        // 2. Search History
        val RECENT_SEARCHES = stringSetPreferencesKey("recent_searches")

        // 3. Appearance & Settings (MỚI THÊM)
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val FONT_SCALE = floatPreferencesKey("font_scale")
    }

    // ==========================================
    // 1. USER INFO
    // ==========================================
    val userName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: "Sinh viên UTH"
        }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USER_NAME] = name
        }
    }

    // Giả lập luồng dữ liệu UserProfile để dùng cho ProfileViewModel
    // (Sau này bạn có thể mở rộng thêm email, avatar...)
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val name = preferences[PreferencesKeys.USER_NAME] ?: "Sinh viên UTH"
            UserPreferences(userName = name)
        }

    // ==========================================
    // 2. CACHING STRATEGY
    // ==========================================
    val lastRefreshTimestamp: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_REFRESH_TIMESTAMP] ?: 0L
        }

    suspend fun updateLastRefreshTimestamp() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_REFRESH_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    // ==========================================
    // 3. SEARCH HISTORY (Tìm kiếm gần đây)
    // ==========================================
    val recentSearches: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val searchesSet = preferences[PreferencesKeys.RECENT_SEARCHES] ?: emptySet()
            // Chuyển Set thành List và đảo ngược để từ mới nhất lên đầu
            searchesSet.toList().reversed()
        }

    suspend fun addRecentSearch(query: String) {
        context.dataStore.edit { preferences ->
            val oldSet = preferences[PreferencesKeys.RECENT_SEARCHES] ?: emptySet()
            val newSet = oldSet.toMutableSet()

            newSet.remove(query) // Xóa cũ để đưa lên đầu
            newSet.add(query)    // Thêm mới

            // Giới hạn 5 từ khóa
            val tempList = newSet.toList()
            val limitedList = if (tempList.size > 5) {
                tempList.subList(tempList.size - 5, tempList.size)
            } else {
                tempList
            }
            preferences[PreferencesKeys.RECENT_SEARCHES] = limitedList.toSet()
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECENT_SEARCHES] = emptySet()
        }
    }

    // ==========================================
    // 4. APPEARANCE & SETTINGS (MỚI THÊM)
    // ==========================================

    // --- Dark Mode ---
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] ?: false
        }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    // --- Language ---
    val languageCode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] ?: "vi"
        }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = code
        }
    }

    // --- Font Scale ---
    val fontScale: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FONT_SCALE] ?: 1.0f
        }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SCALE] = scale
        }
    }
}

// Data class phụ trợ để gom nhóm thông tin User (dùng cho ProfileViewModel)
data class UserPreferences(
    val userName: String
)