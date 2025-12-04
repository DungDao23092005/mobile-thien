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
import javax.inject.Inject

// 🟢 Cập nhật Model: Dùng timestamp (Long) thay vì timeDisplay (String)
data class NotificationUIModel(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long, // 🆕 Thay đổi ở đây
    val type: String,
    val isRead: Boolean,
    val relatedId: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationUIModel>> = repository.getNotifications()
        .map { entities ->
            entities
                .sortedByDescending { it.timestamp }
                .map { entity ->
                    NotificationUIModel(
                        id = entity.id,
                        title = entity.title,
                        message = entity.message,
                        timestamp = entity.timestamp, // 🆕 Truyền thẳng timestamp
                        type = entity.type,
                        isRead = entity.isRead,
                        relatedId = entity.relatedId
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

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }
}