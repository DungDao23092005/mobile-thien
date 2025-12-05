package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.Report
import com.example.stushare.core.data.models.UserEntity
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ==========================================
// DATA MODELS
// ==========================================
data class AdminStats(
    val userCount: Long = 0,
    val documentCount: Long = 0,
    val requestCount: Long = 0
)

// ==========================================
// INTERFACE
// ==========================================
interface AdminRepository {
    suspend fun getSystemStats(): AdminStats
    suspend fun getPendingReports(): Result<List<Report>>
    suspend fun deleteDocumentAndResolveReport(documentId: String, reportId: String): Result<Unit>
    suspend fun dismissReport(reportId: String): Result<Unit>

    // 🟢 MỚI: Quản lý User
    suspend fun getAllUsers(): Result<List<UserEntity>>
    suspend fun toggleUserBanStatus(userId: String, isBanned: Boolean): Result<Unit>
}

// ==========================================
// IMPLEMENTATION
// ==========================================
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AdminRepository {

    override suspend fun getSystemStats(): AdminStats {
        return try {
            val usersQuery = firestore.collection("users").count().get(AggregateSource.SERVER)
            val docsQuery = firestore.collection("documents").count().get(AggregateSource.SERVER)
            val requestsQuery = firestore.collection("requests").count().get(AggregateSource.SERVER)

            val userSnapshot = usersQuery.await()
            val docSnapshot = docsQuery.await()
            val reqSnapshot = requestsQuery.await()

            AdminStats(
                userCount = userSnapshot.count,
                documentCount = docSnapshot.count,
                requestCount = reqSnapshot.count
            )
        } catch (e: Exception) {
            AdminStats()
        }
    }

    override suspend fun getPendingReports(): Result<List<Report>> {
        return try {
            val snapshot = firestore.collection("reports")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp")
                .get()
                .await()

            val reports = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Report::class.java)?.copy(id = doc.id)
            }
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDocumentAndResolveReport(documentId: String, reportId: String): Result<Unit> {
        return try {
            val docSnapshot = firestore.collection("documents").document(documentId).get().await()

            if (docSnapshot.exists()) {
                val fileUrl = docSnapshot.getString("fileUrl")
                val imageUrl = docSnapshot.getString("imageUrl")

                if (!fileUrl.isNullOrBlank() && fileUrl.startsWith("http")) {
                    try {
                        storage.getReferenceFromUrl(fileUrl).delete().await()
                    } catch (e: Exception) { }
                }

                if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http") && !imageUrl.contains("picsum")) {
                    try {
                        storage.getReferenceFromUrl(imageUrl).delete().await()
                    } catch (e: Exception) { }
                }
            }

            firestore.runTransaction { transaction ->
                val docRef = firestore.collection("documents").document(documentId)
                val reportRef = firestore.collection("reports").document(reportId)
                transaction.delete(docRef)
                transaction.update(reportRef, "status", "resolved")
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun dismissReport(reportId: String): Result<Unit> {
        return try {
            firestore.collection("reports").document(reportId)
                .update("status", "dismissed")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 MỚI: Lấy danh sách user
    override suspend fun getAllUsers(): Result<List<UserEntity>> {
        return try {
            val snapshot = firestore.collection("users")
                .orderBy("email")
                .get()
                .await()
            
            // Firebase tự động map field "banned" -> property "isBanned" trong UserEntity
            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserEntity::class.java)?.copy(id = doc.id)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🟢 MỚI: Đổi trạng thái Ban/Unban
    override suspend fun toggleUserBanStatus(userId: String, isBanned: Boolean): Result<Unit> {
        return try {
            // 🔴 QUAN TRỌNG: Sửa key "isBanned" thành "banned" để khớp với mapper
            firestore.collection("users").document(userId)
                .update("banned", isBanned) 
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}