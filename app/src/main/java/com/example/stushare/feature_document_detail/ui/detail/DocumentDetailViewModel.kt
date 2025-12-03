package com.example.stushare.features.feature_document_detail.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.utils.AndroidDownloader
import com.example.stushare.core.utils.AndroidFileOpener
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val document: Document) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val downloader: AndroidDownloader,
    private val fileOpener: AndroidFileOpener,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked = _isBookmarked.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val comments: StateFlow<List<CommentEntity>> = _comments.asStateFlow()

    private val _isSendingComment = MutableStateFlow(false)
    val isSendingComment = _isSendingComment.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    val currentUserId = auth.currentUser?.uid

    fun getDocumentById(documentId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            repository.getDocumentById(documentId).collect { documentFromDb ->
                if (documentFromDb == null) {
                    _uiState.value = DetailUiState.Error("Đang tải dữ liệu...")
                    repository.refreshDocumentsIfStale()
                } else {
                    _uiState.value = DetailUiState.Success(documentFromDb)
                    checkIfBookmarked(documentId)
                    getComments(documentId)
                }
            }
        }
    }

    private fun getComments(documentId: String) {
        viewModelScope.launch {
            repository.getComments(documentId).collect { list ->
                _comments.value = list
            }
        }
    }

    fun sendComment(documentId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _isSendingComment.value = true
            val result = repository.sendComment(documentId, content)
            _isSendingComment.value = false
            if (result.isFailure) {
                _snackbarEvent.emit("Gửi thất bại: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun deleteComment(documentId: String, commentId: String) {
        viewModelScope.launch {
            val result = repository.deleteComment(documentId, commentId)
            if (result.isSuccess) {
                _snackbarEvent.emit("Đã xóa bình luận")
            } else {
                _snackbarEvent.emit("Xóa thất bại: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun checkIfBookmarked(documentId: String) {
        viewModelScope.launch {
            val result = repository.isDocumentBookmarked(documentId)
            if (result.isSuccess) _isBookmarked.value = result.getOrDefault(false)
        }
    }

    fun onBookmarkClick(documentId: String) {
        viewModelScope.launch {
            val newState = !_isBookmarked.value
            _isBookmarked.value = newState
            val result = repository.toggleBookmark(documentId, newState)
            if (result.isFailure) {
                _isBookmarked.value = !newState
                _snackbarEvent.emit("Lỗi: Không thể lưu trạng thái")
            } else {
                val msg = if (newState) "Đã lưu vào danh sách xem sau" else "Đã bỏ lưu"
                _snackbarEvent.emit(msg)
            }
        }
    }

    fun startDownload(documentId: String, url: String, title: String, authorId: String?) {
        if (url.isBlank()) {
            viewModelScope.launch { _snackbarEvent.emit("Lỗi: Link tải bị hỏng") }
            return
        }
        val fileExtension = getFileExtension(url)
        val fileName = "[StuShare] $title.$fileExtension"
        downloader.downloadFile(url, fileName)
        viewModelScope.launch {
            _snackbarEvent.emit("Đang bắt đầu tải xuống...")
            repository.incrementDownloadCount(documentId, authorId, title)
        }
    }

    fun openDocumentOnline(url: String) {
        fileOpener.openFile(url)
    }

    fun onRateDocument(documentId: String, rating: Int) {
        viewModelScope.launch {
            val result = repository.rateDocument(documentId, rating)
            if (result.isSuccess) {
                _snackbarEvent.emit("Cảm ơn bạn đã đánh giá $rating sao! ⭐️")
                getDocumentById(documentId)
            } else {
                _snackbarEvent.emit("Lỗi đánh giá: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // 🟢 MỚI: Hàm xử lý báo cáo
    fun onReportDocument(documentId: String, documentTitle: String, reason: String) {
        viewModelScope.launch {
            val result = repository.reportDocument(documentId, documentTitle, reason)
            if (result.isSuccess) {
                _snackbarEvent.emit("Đã gửi báo cáo. Cảm ơn đóng góp của bạn! ✅")
            } else {
                _snackbarEvent.emit("Lỗi gửi báo cáo: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun getFileExtension(url: String): String {
        return when {
            url.contains(".pdf", ignoreCase = true) -> "pdf"
            url.contains(".doc", ignoreCase = true) -> "docx"
            url.contains(".ppt", ignoreCase = true) -> "pptx"
            url.contains(".xls", ignoreCase = true) -> "xlsx"
            else -> "bin"
        }
    }
}