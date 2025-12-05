package com.example.stushare.features.feature_admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.Report
import com.example.stushare.core.data.models.UserEntity
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

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports = _reports.asStateFlow()

    private val _userList = MutableStateFlow<List<UserEntity>>(emptyList())
    val userList = _userList.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    init {
        loadStats()
        loadReports()
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

    fun loadReports() {
        viewModelScope.launch {
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

    // --- QUẢN LÝ USER ---

    fun loadUsers() {
        viewModelScope.launch {
            if (_userList.value.isEmpty()) _isProcessing.value = true
            
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
            // 🟢 LOGIC: Nếu đang Cấm (true) -> Mở (false). Nếu đang Mở (false) -> Cấm (true).
            val newStatus = !user.isBanned
            val actionMsg = if (newStatus) "đã bị KHÓA" else "đã được MỞ KHÓA"

            // 1. Cập nhật UI ngay lập tức
            val updatedList = _userList.value.map { currentUser ->
                if (currentUser.id == user.id) {
                    currentUser.copy(isBanned = newStatus)
                } else {
                    currentUser
                }
            }
            _userList.value = updatedList

            // 2. Gửi lên Server (Lưu ý: Repository phải dùng key "banned" như đã sửa ở bước trước)
            adminRepository.toggleUserBanStatus(user.id, newStatus)
                .onSuccess {
                    _toastMessage.emit("Tài khoản ${user.email} $actionMsg")
                }
                .onFailure { e ->
                    _toastMessage.emit("Thất bại: ${e.message}")
                    // Hoàn tác nếu lỗi
                    val revertedList = _userList.value.map { currentUser ->
                        if (currentUser.id == user.id) {
                            currentUser.copy(isBanned = !newStatus)
                        } else {
                            currentUser
                        }
                    }
                    _userList.value = revertedList
                }
        }
    }
}