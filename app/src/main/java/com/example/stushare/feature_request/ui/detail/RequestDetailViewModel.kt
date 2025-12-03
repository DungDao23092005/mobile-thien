package com.example.stushare.feature_request.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.core.data.repository.RequestRepository
import com.example.stushare.core.navigation.NavRoute
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.navigation.toRoute
import javax.inject.Inject

data class RequestDetailUiState(
    val request: DocumentRequest? = null,
    val comments: List<CommentEntity> = emptyList(),
    val isLoadingRequest: Boolean = true,
    val isSending: Boolean = false,
    val currentUserId: String = ""
)

@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    savedStateHandle: SavedStateHandle,
    firebaseAuth: FirebaseAuth
) : ViewModel() {

    // Lấy ID từ Navigation Argument một cách an toàn
    private val args = savedStateHandle.toRoute<NavRoute.RequestDetail>()
    private val requestId = args.requestId
    private val currentUserId = firebaseAuth.currentUser?.uid ?: ""

    private val _commentText = MutableStateFlow("")
    val commentText = _commentText.asStateFlow()

    private val _isSending = MutableStateFlow(false)

    // Kết hợp luồng dữ liệu Request và Comment
    val uiState: StateFlow<RequestDetailUiState> = combine(
        requestRepository.getRequestById(requestId),
        requestRepository.getCommentsForRequest(requestId),
        _isSending
    ) { request, comments, isSending ->
        RequestDetailUiState(
            request = request,
            comments = comments,
            isLoadingRequest = request == null, // Nếu chưa load được request thì hiện loading
            isSending = isSending,
            currentUserId = currentUserId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RequestDetailUiState(currentUserId = currentUserId)
    )

    fun onCommentChange(text: String) {
        _commentText.value = text
    }

    fun sendComment() {
        val content = _commentText.value.trim()
        if (content.isBlank()) return

        viewModelScope.launch {
            _isSending.value = true
            try {
                requestRepository.addCommentToRequest(requestId, content)
                _commentText.value = "" // Xóa ô nhập sau khi gửi
            } catch (e: Exception) {
                // Xử lý lỗi nếu cần (VD: hiện Toast)
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }
}