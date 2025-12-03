package com.example.stushare.features.feature_profile.ui.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String? = null,
    val major: String = "Chưa cập nhật",
    val bio: String = "",

    // 🟢 MỚI: Phân quyền (mặc định là 'user', admin sẽ là 'admin')
    val role: String = "user"
)