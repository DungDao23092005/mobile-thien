package com.example.stushare.features.feature_admin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.Report // 🟢 Import Model Report
import com.example.stushare.core.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Giữ nguyên State cũ cho phần thống kê
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

    // 1. State cho Thống kê (Dashboard Stats)
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState = _uiState.asStateFlow()

    // 2. State cho Danh sách Báo cáo (Report List) - 🟢 MỚI
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports = _reports.asStateFlow()

    // 3. Sự kiện thông báo (Toast) - 🟢 MỚI
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // 4. Loading riêng cho các thao tác xử lý report (để không ảnh hưởng UI thống kê)
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    init {
        loadStats()
        loadReports() // 🟢 Gọi thêm hàm tải danh sách báo cáo
    }

    // ==========================================
    // PHẦN CŨ: THỐNG KÊ
    // ==========================================
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

    // ==========================================
    // PHẦN MỚI: QUẢN LÝ BÁO CÁO
    // ==========================================

    fun loadReports() {
        viewModelScope.launch {
            // Chỉ hiện loading nếu danh sách đang rỗng (lần đầu tải)
            if (_reports.value.isEmpty()) _isProcessing.value = true

            adminRepository.getPendingReports()
                .onSuccess { list ->
                    _reports.value = list
                }
                .onFailure { e ->
                    _toastMessage.emit("Lỗi tải báo cáo: ${e.message}")
                }
            _isProcessing.value = false
        }
    }

    // Xóa tài liệu vi phạm
    fun deleteDocument(docId: String, reportId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            adminRepository.deleteDocumentAndResolveReport(docId, reportId)
                .onSuccess {
                    _toastMessage.emit("Đã xóa tài liệu và xử lý báo cáo ✅")
                    // Tải lại dữ liệu để cập nhật danh sách và số lượng
                    loadReports()
                    loadStats()
                }
                .onFailure { e ->
                    _toastMessage.emit("Lỗi xóa: ${e.message}")
                }
            _isProcessing.value = false
        }
    }

    // Bỏ qua báo cáo (giữ lại tài liệu)
    fun dismissReport(reportId: String) {
        viewModelScope.launch {
            adminRepository.dismissReport(reportId)
                .onSuccess {
                    _toastMessage.emit("Đã bỏ qua báo cáo này")
                    loadReports() // Refresh list
                }
                .onFailure { e ->
                    _toastMessage.emit("Lỗi: ${e.message}")
                }
        }
    }
}