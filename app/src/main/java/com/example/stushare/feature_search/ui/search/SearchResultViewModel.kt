package com.example.stushare.feature_search.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Lấy từ khóa từ màn hình trước truyền sang
    val query: String = savedStateHandle.get<String>("query") ?: ""

    // Biến để quản lý luồng tìm kiếm (giúp hủy tìm kiếm cũ nếu gọi lại)
    private var searchJob: Job? = null

    init {
        if (query.isNotBlank()) {
            performSearch(query)
            saveRecentSearch(query)
        } else {
            _uiState.value = SearchUiState.Error("Không nhận được từ khóa tìm kiếm.")
        }
    }

    private fun saveRecentSearch(query: String) {
        viewModelScope.launch {
            settingsRepository.addRecentSearch(query)
        }
    }

    fun performSearch(query: String) {
        // 1. Hủy job cũ nếu đang chạy (tránh xung đột)
        searchJob?.cancel()

        // 2. Bắt đầu job mới
        searchJob = viewModelScope.launch {

            // A. Cố gắng làm mới dữ liệu từ Server (nếu cần)
            // Chạy cái này trong try-catch riêng để nếu mất mạng thì vẫn tìm được offline
            try {
                repository.refreshDocumentsIfStale()
            } catch (e: Exception) {
                e.printStackTrace()
                // Không set Error ở đây, để code chạy tiếp xuống lấy data offline
            }

            // B. Lắng nghe dữ liệu từ Database (Flow)
            // 🟢 QUAN TRỌNG: Dùng collect thay vì gán trực tiếp
            repository.searchDocuments(query.trim())
                .onStart {
                    // Khi bắt đầu tìm thì hiện Loading
                    _uiState.value = SearchUiState.Loading
                }
                .catch { e ->
                    // Xử lý lỗi nếu quá trình lấy tin từ DB bị fail
                    e.printStackTrace()
                    val errorMessage = when (e) {
                        is DataFailureException.NetworkError -> "Mất kết nối mạng"
                        else -> "Lỗi tìm kiếm: ${e.message}"
                    }
                    _uiState.value = SearchUiState.Error(errorMessage)
                }
                .collect { results ->
                    // 🟢 Khi có kết quả (hoặc khi có bài bị xóa), code này tự chạy lại
                    if (results.isEmpty()) {
                        _uiState.value = SearchUiState.Empty
                    } else {
                        _uiState.value = SearchUiState.Success(results, results.size)
                    }
                }
        }
    }
}