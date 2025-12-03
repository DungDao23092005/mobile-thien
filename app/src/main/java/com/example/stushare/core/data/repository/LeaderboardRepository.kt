package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.UserEntity
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    // Lấy danh sách Top người dùng
    fun getTopUsers(): Flow<List<UserEntity>>

    // Lấy danh sách Top tài liệu (dựa trên lượt tải)
    fun getTopDocuments(): Flow<List<Document>>

    // Hàm cập nhật điểm cho User (dùng khi họ upload tài liệu thành công)
    suspend fun updateUserContribution(userId: String, points: Int)

    // --- BẠN CẦN THÊM DÒNG NÀY ---
    suspend fun refreshLeaderboard()
}