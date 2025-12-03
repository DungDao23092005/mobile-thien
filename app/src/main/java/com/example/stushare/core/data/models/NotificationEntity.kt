package com.example.stushare.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val message: String,

    // Mặc định lấy thời gian hiện tại
    val timestamp: Long = System.currentTimeMillis(),

    val userId: String,     // ID người nhận thông báo
    val type: String,       // Loại thông báo (UPLOAD, DOWNLOAD, RATING, COMMENT...)
    val isRead: Boolean = false,

    // ID của đối tượng liên quan (VD: ID tài liệu)
    val relatedId: String? = null
) {
    // Định nghĩa các loại thông báo
    companion object {
        const val TYPE_UPLOAD = "UPLOAD"     // Thông báo khi tự đăng bài thành công
        const val TYPE_DOWNLOAD = "DOWNLOAD" // Thông báo khi có người tải bài của mình
        const val TYPE_SYSTEM = "SYSTEM"     // Thông báo từ hệ thống
        const val TYPE_RATING = "RATING"     // Thông báo khi có người đánh giá

        // 🟢 MỚI: Loại thông báo khi có bình luận
        const val TYPE_COMMENT = "COMMENT"
    }
}