package com.example.stushare.features.feature_home.ui.viewall

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.utils.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

// UiState
sealed interface ViewAllUiState {
    data object Loading : ViewAllUiState
    data class Success(val documents: List<Document>) : ViewAllUiState
    data class Error(val message: String) : ViewAllUiState
}

@HiltViewModel
class ViewAllViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<ViewAllUiState> =
        MutableStateFlow(ViewAllUiState.Loading)

    val uiState: StateFlow<ViewAllUiState> = _uiState.asStateFlow()

    /**
     * Tải tài liệu theo DANH MỤC
     */
    fun loadCategory(category: String) {
        Log.e("VIEWMODEL_TEST", "--- ĐANG CHẠY HÀM loadCategory VỚI: $category ---")

        viewModelScope.launch {
            val databaseType = when (category) {
                AppConstants.CATEGORY_NEW_UPLOADS -> AppConstants.TYPE_BOOK
                AppConstants.CATEGORY_EXAM_PREP -> AppConstants.TYPE_EXAM_PREP
                else -> ""
            }

            // 1. Cố gắng refresh dữ liệu từ API (Network)
            try {
                repository.refreshDocumentsIfStale()
            } catch (e: Exception) {
                // Nếu lỗi mạng thì chỉ log, không chặn luồng hiển thị offline
                e.printStackTrace()
            }

            // 2. Lấy dữ liệu từ Database (Flow) - Realtime update
            repository.getDocumentsByType(databaseType)
                .onStart { _uiState.value = ViewAllUiState.Loading } // Hiện loading khi bắt đầu
                .catch { e ->
                    // Xử lý lỗi khi đọc DB
                    _uiState.value = ViewAllUiState.Error(e.message ?: "Lỗi đọc dữ liệu nội bộ")
                }
                .collect { documentsFromDb ->
                    // ✅ THÀNH CÔNG: Flow trả về List -> Cập nhật UI
                    _uiState.value = ViewAllUiState.Success(documentsFromDb)
                }
        }
    }

    /**
     * Tải tài liệu theo TỪ KHÓA (Search)
     */
    fun search(query: String) {
        Log.e("VIEWMODEL_TEST", "--- ĐANG CHẠY HÀM search VỚI: $query ---")

        viewModelScope.launch {
            // 1. Refresh dữ liệu (Network)
            try {
                repository.refreshDocumentsIfStale()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Tìm kiếm trong Database (Flow)
            // 🔴 CŨ (LỖI): val searchResults = repository.searchDocuments(query)
            // 🟢 MỚI (ĐÚNG): Dùng .collect để lắng nghe Flow
            repository.searchDocuments(query)
                .onStart { _uiState.value = ViewAllUiState.Loading }
                .catch { e ->
                    val errorMessage = when (e) {
                        is DataFailureException.NetworkError -> "Lỗi kết nối mạng."
                        else -> e.message ?: "Lỗi tìm kiếm."
                    }
                    _uiState.value = ViewAllUiState.Error(errorMessage)
                }
                .collect { searchResults ->
                    // ✅ THÀNH CÔNG: Cập nhật UI mỗi khi danh sách thay đổi
                    _uiState.value = ViewAllUiState.Success(searchResults)
                }
        }
    }
}