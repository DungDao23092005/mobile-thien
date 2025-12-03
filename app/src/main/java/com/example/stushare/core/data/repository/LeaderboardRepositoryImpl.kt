package com.example.stushare.core.data.repository

import android.util.Log
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.db.UserDao
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LeaderboardRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val documentDao: DocumentDao,
    private val firestore: FirebaseFirestore
) : LeaderboardRepository {

    override fun getTopUsers(): Flow<List<UserEntity>> {
        // Lấy dữ liệu từ Local Database (Room)
        return userDao.getTopUsers()
    }

    override fun getTopDocuments(): Flow<List<Document>> {
        return documentDao.getTopDocuments()
    }

    override suspend fun updateUserContribution(userId: String, points: Int) {
        // 1. Cập nhật vào Room (Local)
        val user = userDao.getUserById(userId)
        if (user != null) {
            val updatedUser = user.copy(contributionPoints = user.contributionPoints + points)
            userDao.insertUser(updatedUser)
        }

        // 2. Cập nhật lên Firestore
        try {
            firestore.collection("users").document(userId)
                .update("contributionPoints", com.google.firebase.firestore.FieldValue.increment(points.toLong()))
                .await()
        } catch (e: Exception) {
            Log.e("LeaderboardRepo", "Lỗi khi cập nhật điểm lên Firestore: ${e.message}")
        }
    }

    override suspend fun refreshLeaderboard() {
        try {
            // 1. Lấy Top 20 user có điểm cao nhất từ Firestore
            val snapshot = firestore.collection("users")
                .orderBy("contributionPoints", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            // 2. Chuyển đổi dữ liệu
            val userList = snapshot.documents.mapNotNull { doc ->
                UserEntity(
                    id = doc.id,
                    fullName = doc.getString("fullName") ?: "Unknown User",
                    // --- ĐÃ SỬA: Thêm email và xóa rank ---
                    email = doc.getString("email") ?: "",
                    avatarUrl = doc.getString("avatarUrl"),
                    contributionPoints = doc.getLong("contributionPoints")?.toInt() ?: 0
                )
            }

            // 3. Lưu vào Room Database
            if (userList.isNotEmpty()) {
                // --- ĐÃ SỬA: Dùng vòng lặp để tránh lỗi nếu DAO chưa có hàm insert list ---
                userList.forEach { user ->
                    userDao.insertUser(user)
                }
            }

        } catch (e: Exception) {
            Log.e("LeaderboardRepo", "Lỗi khi tải bảng xếp hạng: ${e.message}")
        }
    }
}