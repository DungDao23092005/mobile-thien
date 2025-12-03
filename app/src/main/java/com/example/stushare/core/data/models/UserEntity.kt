package com.example.stushare.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,                 // ID người dùng (khớp với Firebase Auth UID)
    val fullName: String,           // Tên hiển thị
    val email: String,              // Email
    val avatarUrl: String? = null,  // Link ảnh đại diện
    val contributionPoints: Int = 0,// Điểm đóng góp (để xếp hạng)
    val uploadedCount: Int = 0      // Số lượng tài liệu đã tải lên
)