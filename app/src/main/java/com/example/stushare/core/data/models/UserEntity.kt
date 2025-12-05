package com.example.stushare.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val contributionPoints: Int = 0,
    val uploadedCount: Int = 0,
    val role: String = "user",
    val isBanned: Boolean = false
)