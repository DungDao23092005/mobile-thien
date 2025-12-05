package com.example.stushare.features.feature_admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.NotificationEntity
import com.example.stushare.core.data.models.Report
import com.example.stushare.core.data.models.UserEntity
import com.example.stushare.core.data.repository.AdminRepository
import com.example.stushare.core.data.repository.NotificationRepository // 🟢 Import
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
    private val adminRepository: AdminRepository,
    private val notificationRepository: NotificationRepository // 🟢 MỚI: Inject thêm cái này
) : ViewModel() {

    // ... (Giữ nguyên các State cũ: _uiState, _reports, _userList, _toastMessage, _isProcessing)
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
        loadUsers() // 🟢 Load sẵn user để dùng tìm kiếm email khi gửi thông báo
    }

    // ... (Giữ nguyên các hàm: loadStats, loadReports, deleteDocument, dismissReport)
    // Bạn copy lại y nguyên code cũ của các hàm trên

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
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            if (_reports.value.isEmpty()) _isProcessing.value = true
            adminRepository.getPendingReports()
                .onSuccess { list -> _reports.value = list }
                .onFailure { }
            _isProcessing.value = false
        }
    }

    fun deleteDocument(docId: String, reportId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            adminRepository.deleteDocumentAndResolveReport(docId, reportId)
                .onSuccess {
                    _toastMessage.emit("Đã xử lý xong ✅")
                    loadReports()
                    loadStats()
                }
                .onFailure { e -> _toastMessage.emit("Lỗi: ${e.message}") }
            _isProcessing.value = false
        }
    }

    fun dismissReport(reportId: String) {
        viewModelScope.launch {
            adminRepository.dismissReport(reportId)
                .onSuccess {
                    _toastMessage.emit("Đã bỏ qua báo cáo")
                    loadReports()
                }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            // Load ngầm, không hiện loading toàn màn hình
            adminRepository.getAllUsers()
                .onSuccess { users -> _userList.value = users }
        }
    }

    fun toggleUserBan(user: UserEntity) {
        viewModelScope.launch {
            val newStatus = !user.isBanned
            val actionMsg = if (newStatus) "đã bị KHÓA" else "đã được MỞ KHÓA"

            // 1. Cập nhật UI ngay lập tức
            val updatedList = _userList.value.map { currentUser ->
                if (currentUser.id == user.id) currentUser.copy(isBanned = newStatus) else currentUser
            }
            _userList.value = updatedList

            // 2. Gửi lên Server
            adminRepository.toggleUserBanStatus(user.id, newStatus)
                .onSuccess { _toastMessage.emit("Tài khoản ${user.email} $actionMsg") }
                .onFailure { e ->
                    _toastMessage.emit("Thất bại: ${e.message}")
                    // Rollback UI nếu lỗi
                    val revertedList = _userList.value.map { currentUser ->
                        if (currentUser.id == user.id) currentUser.copy(isBanned = !newStatus) else currentUser
                    }
                    _userList.value = revertedList
                }
        }
    }

    // 🟢 MỚI: HÀM GỬI THÔNG BÁO HỆ THỐNG
    fun sendSystemNotification(
        title: String,
        content: String,
        isSendToAll: Boolean,
        targetEmail: String
    ) {
        if (title.isBlank() || content.isBlank()) {
            viewModelScope.launch { _toastMessage.emit("Vui lòng nhập tiêu đề và nội dung") }
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            
            if (isSendToAll) {
                // Gửi cho TẤT CẢ
                val users = _userList.value.ifEmpty {
                    adminRepository.getAllUsers().getOrDefault(emptyList())
                }

                if (users.isNotEmpty()) {
                    var count = 0
                    users.forEach { user ->
                        notificationRepository.createNotification(
                            targetUserId = user.id,
                            title = title,
                            message = content,
                            type = NotificationEntity.TYPE_SYSTEM,
                            relatedId = null
                        )
                        count++
                    }
                    _toastMessage.emit("Đã gửi cho $count người dùng!")
                } else {
                    _toastMessage.emit("Danh sách người dùng trống!")
                }

            } else {
                // Gửi cho CÁ NHÂN (Tìm theo Email)
                val targetUser = _userList.value.find { it.email == targetEmail.trim() }
                
                if (targetUser != null) {
                    notificationRepository.createNotification(
                        targetUserId = targetUser.id,
                        title = title,
                        message = content,
                        type = NotificationEntity.TYPE_SYSTEM,
                        relatedId = null
                    )
                    _toastMessage.emit("Đã gửi cho ${targetUser.fullName}")
                } else {
                    _toastMessage.emit("Không tìm thấy email: $targetEmail")
                }
            }
            _isProcessing.value = false
        }
    }
}