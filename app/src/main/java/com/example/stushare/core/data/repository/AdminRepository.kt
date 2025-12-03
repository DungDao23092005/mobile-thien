package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.Report // 🟢 Nhớ import Model Report
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage // 🟢 Thêm cái này để xóa file
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// ==========================================
// 1. DATA MODELS
// ==========================================

// Data Model cho thống kê (Giữ nguyên của bạn)
data class AdminStats(
    val userCount: Long = 0,
    val documentCount: Long = 0,
    val requestCount: Long = 0
)

// ==========================================
// 2. INTERFACE
// ==========================================

interface AdminRepository {
    // --- Phần cũ: Thống kê ---
    suspend fun getSystemStats(): AdminStats

    // --- Phần mới: Quản lý Báo cáo ---
    suspend fun getPendingReports(): Result<List<Report>>
    suspend fun deleteDocumentAndResolveReport(documentId: String, reportId: String): Result<Unit>
    suspend fun dismissReport(reportId: String): Result<Unit>
}

// ==========================================
// 3. IMPLEMENTATION
// ==========================================

class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage // 🟢 Inject thêm Storage vào đây
) : AdminRepository {

    // ----------------------------------------------------------------
    // LOGIC CŨ: THỐNG KÊ
    // ----------------------------------------------------------------
    override suspend fun getSystemStats(): AdminStats {
        return try {
            // AggregateSource.SERVER: Đếm trực tiếp trên server (Tiết kiệm băng thông)
            val usersQuery = firestore.collection("users").count().get(AggregateSource.SERVER)
            val docsQuery = firestore.collection("documents").count().get(AggregateSource.SERVER)
            val requestsQuery = firestore.collection("requests").count().get(AggregateSource.SERVER)

            // Chờ kết quả song song
            val userSnapshot = usersQuery.await()
            val docSnapshot = docsQuery.await()
            val reqSnapshot = requestsQuery.await()

            AdminStats(
                userCount = userSnapshot.count,
                documentCount = docSnapshot.count,
                requestCount = reqSnapshot.count
            )
        } catch (e: Exception) {
            // Trả về mặc định nếu lỗi mạng
            AdminStats()
        }
    }

    // ----------------------------------------------------------------
    // LOGIC MỚI: QUẢN LÝ BÁO CÁO
    // ----------------------------------------------------------------

    override suspend fun getPendingReports(): Result<List<Report>> {
        return try {
            val snapshot = firestore.collection("reports")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp") // Xử lý cái cũ trước
                .get()
                .await()

            val reports = snapshot.documents.map { doc ->
                // Ép kiểu về Report object và gán ID của document report vào
                doc.toObject(Report::class.java)!!.copy(id = doc.id)
            }
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDocumentAndResolveReport(documentId: String, reportId: String): Result<Unit> {
        return try {
            // 1. Lấy thông tin tài liệu để lấy URL file/ảnh (cần để xóa trên Storage)
            val docSnapshot = firestore.collection("documents").document(documentId).get().await()

            if (docSnapshot.exists()) {
                val fileUrl = docSnapshot.getString("fileUrl")
                val imageUrl = docSnapshot.getString("imageUrl")

                // 2. Xóa File PDF/Word trên Storage (nếu có và link hợp lệ)
                if (!fileUrl.isNullOrBlank() && fileUrl.startsWith("http")) {
                    try {
                        val fileRef = storage.getReferenceFromUrl(fileUrl)
                        fileRef.delete().await()
                    } catch (e: Exception) {
                        // Bỏ qua lỗi xóa file (ví dụ file đã bị xóa thủ công trước đó)
                        // để không chặn quy trình xóa data
                    }
                }

                // 3. Xóa Ảnh bìa trên Storage (trừ ảnh mặc định picsum)
                if (!imageUrl.isNullOrBlank() && imageUrl.startsWith("http") && !imageUrl.contains("picsum")) {
                    try {
                        val imgRef = storage.getReferenceFromUrl(imageUrl)
                        imgRef.delete().await()
                    } catch (e: Exception) { /* Bỏ qua */ }
                }
            }

            // 4. Transaction: Xóa doc trong Firestore & Cập nhật Report cùng lúc
            firestore.runTransaction { transaction ->
                val docRef = firestore.collection("documents").document(documentId)
                val reportRef = firestore.collection("reports").document(reportId)

                // Xóa document
                transaction.delete(docRef)

                // Đánh dấu report đã giải quyết
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
                .update("status", "dismissed") // Đánh dấu là đã xem/bỏ qua
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}