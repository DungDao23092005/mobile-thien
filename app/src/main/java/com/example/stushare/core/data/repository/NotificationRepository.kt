package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.NotificationEntity
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    // Lấy danh sách thông báo
    fun getNotifications(): Flow<List<NotificationEntity>>

    // Lấy số lượng tin chưa đọc
    fun getUnreadCount(): Flow<Int>

    // Tạo thông báo mới
    // 🆕 CẬP NHẬT: Thêm tham số relatedId (để biết link tới tài liệu nào)
    suspend fun createNotification(
        targetUserId: String,
        title: String,
        message: String,
        type: String,
        relatedId: String? = null // Cho phép null
    )

    // Các hàm thao tác khác giữ nguyên
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
}