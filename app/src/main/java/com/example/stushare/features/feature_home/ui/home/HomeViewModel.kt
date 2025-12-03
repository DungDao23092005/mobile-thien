package com.example.stushare.features.feature_home.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DataFailureException
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.DocumentRequest // 🟢 Import Model Yêu cầu
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.repository.NotificationRepository
import com.example.stushare.core.data.repository.RequestRepository // 🟢 Import Repo Yêu cầu
import com.example.stushare.core.domain.usecase.GetExamDocumentsUseCase
import com.example.stushare.core.domain.usecase.GetNewDocumentsUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "Sinh Viên",
    val avatarUrl: String? = null,
    val newDocuments: List<Document> = emptyList(),
    val examDocuments: List<Document> = emptyList(),
    val bookDocuments: List<Document> = emptyList(),
    val lectureDocuments: List<Document> = emptyList(),

    // 🟢 MỚI: Danh sách yêu cầu từ cộng đồng
    val requestDocuments: List<DocumentRequest> = emptyList(),

    val unreadNotificationCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val getNewDocumentsUseCase: GetNewDocumentsUseCase,
    private val getExamDocumentsUseCase: GetExamDocumentsUseCase,
    private val notificationRepository: NotificationRepository,
    private val requestRepository: RequestRepository, // 🟢 Inject thêm Repo này
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Tổng hợp tất cả các luồng dữ liệu (Bây giờ là 9 luồng)
    val uiState: StateFlow<HomeUiState> = combine(
        getNewDocumentsUseCase().catch { emit(emptyList()) },       // 0
        getExamDocumentsUseCase().catch { emit(emptyList()) },      // 1
        repository.getDocumentsByType("book").catch { emit(emptyList()) }, // 2
        repository.getDocumentsByType("lecture").catch { emit(emptyList()) }, // 3
        _isLoading,                                                 // 4
        _isRefreshing,                                              // 5
        _errorMessage,                                              // 6
        notificationRepository.getUnreadCount().catch { emit(0) }.onStart { emit(0) }, // 7
        requestRepository.getAllRequests().catch { emit(emptyList()) } // 8 🟢 Luồng Yêu cầu
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val newDocs = args[0] as List<Document>
        @Suppress("UNCHECKED_CAST")
        val examDocs = args[1] as List<Document>
        @Suppress("UNCHECKED_CAST")
        val bookDocs = args[2] as List<Document>
        @Suppress("UNCHECKED_CAST")
        val lectureDocs = args[3] as List<Document>

        val isLoading = args[4] as Boolean
        val isRefreshing = args[5] as Boolean
        val error = args[6] as? String
        val unreadCount = args[7] as Int

        @Suppress("UNCHECKED_CAST")
        val requests = args[8] as List<DocumentRequest> // 🟢 Lấy danh sách yêu cầu

        val currentUser = firebaseAuth.currentUser
        val name = currentUser?.displayName ?: "Sinh Viên"
        val avatar = currentUser?.photoUrl?.toString()

        HomeUiState(
            userName = name,
            avatarUrl = avatar,
            newDocuments = newDocs,
            examDocuments = examDocs,
            bookDocuments = bookDocs,
            lectureDocuments = lectureDocs,
            requestDocuments = requests.take(10), // 🟢 Chỉ lấy 10 yêu cầu mới nhất để hiển thị Home
            unreadNotificationCount = unreadCount,
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        loadData(isInitial = true)
    }

    fun refreshData() {
        loadData(isInitial = false)
    }

    private fun loadData(isInitial: Boolean) {
        viewModelScope.launch {
            _errorMessage.value = null
            if (isInitial) _isLoading.value = true else _isRefreshing.value = true

            try {
                repository.refreshDocumentsIfStale()
                // RequestRepository dùng Realtime flow nên không cần refresh thủ công
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = when (e) {
                    is java.io.IOException -> "Vui lòng kiểm tra kết nối mạng."
                    is DataFailureException.NetworkError -> "Lỗi kết nối máy chủ."
                    else -> "Không thể cập nhật dữ liệu mới nhất."
                }
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}