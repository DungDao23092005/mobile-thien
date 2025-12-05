package com.example.stushare.features.feature_admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.Report
import com.example.stushare.core.data.models.UserEntity // 🟢 Import
import com.example.stushare.core.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val userCount: String = "-",
    val docCount: String = "-",
    val requestCount: String = "-",
    val isLoading: Boolean = true
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    // 1. State cho Thống kê
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    // 2. State cho Báo cáo
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports = _reports.asStateFlow()

    // 3. State cho Danh sách User - 🟢 MỚI
    private val _userList = MutableStateFlow<List<UserEntity>>(emptyList())
    val userList = _userList.asStateFlow()

    // 4. Các biến chung
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    init {
        loadStats()
        loadReports()
        // Không load users ngay lập tức để tiết kiệm, sẽ gọi khi vào màn UserList
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val stats = adminRepository.getSystemStats()
                _uiState.value = AdminUiState(
                    userCount = stats.userCount.toString(),
                    docCount = stats.documentCount.toString(),
                    requestCount = stats.requestCount.toString(),
                    isLoading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // --- REPORT LOGIC ---
    fun loadReports() {
        viewModelScope.launch {
            // Chỉ hiện loading nếu danh sách rỗng
            if (_reports.value.isEmpty()) _isProcessing.value = true
            adminRepository.getPendingReports()
                .onSuccess { list -> _reports.value = list }
                .onFailure { e -> _toastMessage.emit("Lỗi tải báo cáo: ${e.message}") }
            _isProcessing.value = false
        }
    }

    fun deleteDocument(docId: String, reportId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            adminRepository.deleteDocumentAndResolveReport(docId, reportId)
                .onSuccess {
                    _toastMessage.emit("Đã xóa tài liệu và xử lý báo cáo ✅")
                    loadReports()
                    loadStats()
                }
                .onFailure { e -> _toastMessage.emit("Lỗi xóa: ${e.message}") }
            _isProcessing.value = false
        }
    }

    fun dismissReport(reportId: String) {
        viewModelScope.launch {
            adminRepository.dismissReport(reportId)
                .onSuccess {
                    _toastMessage.emit("Đã bỏ qua báo cáo này")
                    loadReports()
                }
                .onFailure { e -> _toastMessage.emit("Lỗi: ${e.message}") }
        }
    }

    // --- USER MANAGEMENT LOGIC - 🟢 MỚI ---

    fun loadUsers() {
        viewModelScope.launch {
            _isProcessing.value = true
            adminRepository.getAllUsers()
                .onSuccess { users ->
                    _userList.value = users
                }
                .onFailure { e ->
                    _toastMessage.emit("Lỗi tải danh sách user: ${e.message}")
                }
            _isProcessing.value = false
        }
    }

    fun toggleUserBan(user: UserEntity) {
        viewModelScope.launch {
            val newStatus = !user.isBanned
            val actionMsg = if (newStatus) "đã bị KHÓA" else "đã được MỞ KHÓA"
            
            adminRepository.toggleUserBanStatus(user.id, newStatus)
                .onSuccess {
                    _toastMessage.emit("Tài khoản ${user.email} $actionMsg")
                    loadUsers() // Load lại danh sách để cập nhật UI
                }
                .onFailure { e ->
                    _toastMessage.emit("Thất bại: ${e.message}")
                }
        }
    }
}