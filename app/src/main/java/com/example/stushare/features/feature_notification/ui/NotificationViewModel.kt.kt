package com.example.stushare.features.feature_notification.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

// 🟢 1. Cập nhật Model hiển thị
data class NotificationUIModel(
    val id: String,
    val title: String,
    val message: String,
    val timeDisplay: String,
    val type: String,
    val isRead: Boolean,
    val relatedId: String? = null // 🆕 MỚI: Thêm trường này để biết cần mở tài liệu nào
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    // 🟢 2. Mapping dữ liệu và Sắp xếp
    val notifications: StateFlow<List<NotificationUIModel>> = repository.getNotifications()
        .map { entities ->
            entities
                .sortedByDescending { it.timestamp } // 🆕 QUAN TRỌNG: Sắp xếp tin mới nhất lên đầu
                .map { entity ->
                    NotificationUIModel(
                        id = entity.id,
                        title = entity.title,
                        message = entity.message,
                        timeDisplay = convertTimestampToRelativeTime(entity.timestamp),
                        type = entity.type,
                        isRead = entity.isRead,
                        relatedId = entity.relatedId // 🆕 Map dữ liệu từ Entity sang UI
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = repository.getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Đánh dấu 1 tin đã đọc
    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    // Đánh dấu tất cả đã đọc
    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    // Xóa thông báo
    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // Hàm tiện ích: Chuyển đổi thời gian
    private fun convertTimestampToRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 0 -> "Vừa xong" // Xử lý trường hợp giờ server bị lệch nhẹ
            diff < 60 * 1000 -> "Vừa xong"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} phút trước"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} giờ trước"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} ngày trước"
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.format(timestamp)
            }
        }
    }
}