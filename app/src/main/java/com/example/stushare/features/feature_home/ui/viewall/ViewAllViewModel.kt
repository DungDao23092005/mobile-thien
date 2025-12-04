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
            // 🟢 SỬA LỖI QUAN TRỌNG: Mapping đúng các từ khóa từ HomeScreen sang Database Type
            val databaseType = when (category) {
                // Các loại tài liệu cụ thể (Phải khớp với trường 'type' trong Firebase/Database)
                "exam_review", "exam_prep", AppConstants.CATEGORY_EXAM_PREP -> "exam_review"
                "book", "Sách" -> "book"
                "lecture", "slide", "Bài giảng" -> "lecture"
                
                // Mặc định: Nếu không khớp các case trên, giữ nguyên giá trị category để tìm
                else -> category 
            }

            // 1. Cố gắng refresh dữ liệu từ API (Network)
            try {
                repository.refreshDocumentsIfStale()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Lấy dữ liệu từ Database (Flow)
            // Nếu là "new_uploads", tạm thời ta lấy type "book" hoặc tất cả (tùy logic app của bạn),
            // ở đây mình để tạm là lấy 'book' nếu là new_uploads để tránh lỗi rỗng.
            val flow = if (category == "new_uploads" || category == AppConstants.CATEGORY_NEW_UPLOADS) {
                 repository.getDocumentsByType("book") 
            } else {
                repository.getDocumentsByType(databaseType)
            }

            flow
                .onStart { _uiState.value = ViewAllUiState.Loading }
                .catch { e ->
                    _uiState.value = ViewAllUiState.Error(e.message ?: "Lỗi đọc dữ liệu nội bộ")
                }
                .collect { documentsFromDb ->
                    _uiState.value = ViewAllUiState.Success(documentsFromDb)
                }
        }
    }

    /**
     * Tải tài liệu theo TỪ KHÓA (Search)
     */
    fun search(query: String) {
        viewModelScope.launch {
            try {
                repository.refreshDocumentsIfStale()
            } catch (e: Exception) {
                e.printStackTrace()
            }

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
                    _uiState.value = ViewAllUiState.Success(searchResults)
                }
        }
    }
}