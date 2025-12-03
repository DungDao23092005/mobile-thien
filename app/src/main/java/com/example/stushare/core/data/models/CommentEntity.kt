package com.example.stushare.core.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CommentEntity(
    val id: String = "",
    val documentId: String = "",
    val userId: String = "",
    val userName: String = "Người dùng ẩn danh",
    val userAvatar: String? = null,
    val content: String = "",

    @ServerTimestamp // Tự động lấy giờ chuẩn của Server Firebase
    val timestamp: Date? = null
)