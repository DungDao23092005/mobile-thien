package com.example.stushare.core.data.repository

import android.util.Log
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.db.UserDao
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LeaderboardRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val documentDao: DocumentDao,
    private val firestore: FirebaseFirestore
) : LeaderboardRepository {

    override fun getTopUsers(): Flow<List<UserEntity>> {
        return userDao.getTopUsers()
    }

    override fun getTopDocuments(): Flow<List<Document>> {
        return documentDao.getTopDocuments()
    }

    override suspend fun updateUserContribution(userId: String, points: Int) {
        // 1. Cập nhật Local
        val user = userDao.getUserById(userId)
        if (user != null) {
            val updatedUser = user.copy(contributionPoints = user.contributionPoints + points)
            userDao.insertUser(updatedUser)
        }

        // 2. Cập nhật Firestore
        try {
            firestore.collection("users").document(userId)
                .update("contributionPoints", com.google.firebase.firestore.FieldValue.increment(points.toLong()))
                .await()
        } catch (e: Exception) {
            Log.e("LeaderboardRepo", "Lỗi update điểm: ${e.message}")
        }
    }

    // --- HÀM TỰ ĐỘNG SỬA LỖI DATABASE ---
    override suspend fun syncMissingFields() {
        try {
            Log.d("LeaderboardFix", "Đang quét lỗi thiếu dữ liệu (Name, Email, Points)...")
            
            val snapshot = firestore.collection("users").get().await()
            val batch: WriteBatch = firestore.batch()
            var fixCount = 0

            for (doc in snapshot.documents) {
                var needsUpdate = false
                
                // 1. Nếu thiếu điểm -> Thêm điểm 0
                if (!doc.contains("contributionPoints")) {
                    batch.update(doc.reference, "contributionPoints", 0)
                    needsUpdate = true
                }

                // 2. Nếu thiếu Tên -> Thêm tên mặc định "Thành viên ..."
                if (!doc.contains("fullName")) {
                    val defaultName = "Thành viên ${doc.id.takeLast(4).uppercase()}"
                    batch.update(doc.reference, "fullName", defaultName)
                    needsUpdate = true
                }

                // 3. Nếu thiếu Email -> Thêm email rỗng
                if (!doc.contains("email")) {
                    batch.update(doc.reference, "email", "")
                    needsUpdate = true
                }

                if (needsUpdate) {
                    fixCount++
                    Log.d("LeaderboardFix", "Phát hiện user lỗi ID: ${doc.id} -> Đã thêm vào hàng đợi sửa.")
                }
            }

            // Thực thi sửa lỗi 1 lần
            if (fixCount > 0) {
                batch.commit().await()
                Log.d("LeaderboardFix", "Đã tự động sửa dữ liệu cho $fixCount user thành công!")
            } else {
                Log.d("LeaderboardFix", "Dữ liệu sạch, không cần sửa gì thêm.")
            }
        } catch (e: Exception) {
            Log.e("LeaderboardFix", "Lỗi sync: ${e.message}")
        }
    }

    override suspend fun refreshLeaderboard() {
        try {
            // Lấy 20 user điểm cao nhất
            val snapshot = firestore.collection("users")
                .orderBy("contributionPoints", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            val userList = snapshot.documents.mapNotNull { doc ->
                // 1. Xử lý điểm an toàn
                val rawPoints = doc.get("contributionPoints")
                val points = when (rawPoints) {
                    is Number -> rawPoints.toInt()
                    is String -> rawPoints.toIntOrNull() ?: 0
                    else -> 0
                }

                // 2. Xử lý hiển thị Tên (Ưu tiên: Tên thật > Email > SĐT > ID)
                val originalName = doc.getString("fullName")?.trim()
                val email = doc.getString("email")?.trim()
                val phone = doc.getString("phone")?.trim()

                // Log kiểm tra
                Log.d("LeaderboardData", "ID: ${doc.id} | Name: $originalName | Phone: $phone")

                val displayName = when {
                    !originalName.isNullOrEmpty() -> originalName   // Có tên thì hiện tên
                    !email.isNullOrEmpty() -> email                 // Không tên thì hiện email
                    !phone.isNullOrEmpty() -> phone                 // Không email thì hiện SĐT
                    else -> "User ${doc.id.take(6)}"                // Cùng đường thì hiện User ID
                }

                UserEntity(
                    id = doc.id,
                    fullName = displayName,
                    email = email ?: "",
                    avatarUrl = doc.getString("avatarUrl"),
                    contributionPoints = points
                )
            }

            // Lưu vào Room
            if (userList.isNotEmpty()) {
                userList.forEach { userDao.insertUser(it) }
                Log.d("LeaderboardRepo", "Đã cập nhật ${userList.size} user vào bảng xếp hạng.")
            }

        } catch (e: Exception) {
            Log.e("LeaderboardRepo", "Lỗi tải BXH: ${e.message}")
        }
    }
}