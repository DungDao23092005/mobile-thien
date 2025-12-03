package com.example.stushare.core.data.repository

import android.util.Log
import com.example.stushare.core.data.db.NotificationDao
import com.example.stushare.core.data.models.NotificationEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : NotificationRepository {

    private var isListening = false
    // 🟢 MỚI: Biến theo dõi User hiện tại đang lắng nghe
    private var currentUserIdListening: String? = null
    // 🟢 MỚI: Biến giữ listener để hủy khi cần thiết (tránh rò rỉ bộ nhớ)
    private var listenerRegistration: ListenerRegistration? = null

    // =========================================================================
    // 1. LẤY DANH SÁCH THÔNG BÁO (REALTIME)
    // =========================================================================
    override fun getNotifications(): Flow<List<NotificationEntity>> {
        val currentUser = auth.currentUser
        if (currentUser == null) return emptyFlow()

        // 🟢 SỬA LỖI QUAN TRỌNG: Kiểm tra nếu User thay đổi -> Reset listener
        if (currentUserIdListening != currentUser.uid) {
            Log.d("NOTIF_DEBUG", "🔄 Phát hiện đổi User (Cũ: $currentUserIdListening -> Mới: ${currentUser.uid}). Reset Listener.")

            // Hủy listener cũ nếu có
            listenerRegistration?.remove()
            isListening = false
            currentUserIdListening = currentUser.uid
        }

        // Kích hoạt lắng nghe Realtime từ Firestore
        startRealtimeSync(currentUser.uid)

        // Trả về dữ liệu từ Local Room
        return notificationDao.getNotifications(currentUser.uid)
    }

    override fun getUnreadCount(): Flow<Int> {
        val userId = auth.currentUser?.uid ?: return emptyFlow()
        return notificationDao.getUnreadCount(userId)
    }

    // =========================================================================
    // 2. TẠO THÔNG BÁO MỚI
    // =========================================================================
    override suspend fun createNotification(
        targetUserId: String,
        title: String,
        message: String,
        type: String,
        relatedId: String?
    ) {
        withContext(Dispatchers.IO) {
            try {
                val newId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()

                // 1. Tạo Entity để lưu Local
                val notificationEntity = NotificationEntity(
                    id = newId,
                    title = title,
                    message = message,
                    timestamp = timestamp,
                    userId = targetUserId,
                    type = type,
                    isRead = false,
                    relatedId = relatedId
                )

                // 2. Tạo Map để lưu Firestore (Đảm bảo có relatedId)
                val firestoreData = hashMapOf(
                    "id" to newId,
                    "title" to title,
                    "message" to message,
                    "timestamp" to timestamp,
                    "userId" to targetUserId,
                    "type" to type,
                    "isRead" to false,
                    "relatedId" to relatedId
                )

                Log.d("NOTIF_DEBUG", "📤 Đang gửi thông báo đến: $targetUserId | relatedId: $relatedId")

                // A. Lưu lên Cloud (Firestore)
                firestore.collection("users")
                    .document(targetUserId)
                    .collection("notifications")
                    .document(newId)
                    .set(firestoreData)
                    .await()

                Log.d("NOTIF_DEBUG", "✅ Gửi thành công lên Cloud")

                // B. Nếu gửi cho chính mình -> Lưu luôn vào Local
                if (targetUserId == auth.currentUser?.uid) {
                    notificationDao.insertNotification(notificationEntity)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("NOTIF_DEBUG", "❌ Lỗi tạo thông báo: ${e.message}")
            }
        }
    }

    // =========================================================================
    // 3. CÁC HÀM THAO TÁC KHÁC
    // =========================================================================
    override suspend fun markAsRead(id: String) {
        withContext(Dispatchers.IO) {
            notificationDao.markAsRead(id)
            updateReadStatusOnCloud(id, true)
        }
    }

    override suspend fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        withContext(Dispatchers.IO) {
            notificationDao.markAllAsRead(userId)
        }
    }

    override suspend fun deleteNotification(id: String) {
        withContext(Dispatchers.IO) {
            notificationDao.deleteNotification(id)
            updateReadStatusOnCloud(id, null) // Null = Xóa
        }
    }

    // =========================================================================
    // 4. PRIVATE HELPERS (Đồng bộ ngầm)
    // =========================================================================

    private fun startRealtimeSync(userId: String) {
        if (isListening) return
        isListening = true
        Log.d("NOTIF_DEBUG", "🎧 Bắt đầu lắng nghe thông báo cho User: $userId")

        // 🟢 Gán listener vào biến để quản lý vòng đời
        listenerRegistration = firestore.collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isListening = false
                    Log.e("NOTIF_DEBUG", "❌ Lỗi lắng nghe Realtime: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    Log.d("NOTIF_DEBUG", "📥 Nhận được ${snapshot.size()} thông báo từ Cloud")

                    CoroutineScope(Dispatchers.IO).launch {
                        val notifications = snapshot.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: ""
                            val message = doc.getString("message") ?: ""
                            val type = doc.getString("type") ?: "SYSTEM"
                            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val isRead = doc.getBoolean("isRead") ?: false
                            val relatedId = doc.getString("relatedId")

                            NotificationEntity(
                                id = id,
                                title = title,
                                message = message,
                                timestamp = timestamp,
                                userId = userId,
                                type = type,
                                isRead = isRead,
                                relatedId = relatedId
                            )
                        }
                        // Lưu danh sách mới vào Room -> UI sẽ tự cập nhật
                        notifications.forEach {
                            notificationDao.insertNotification(it)
                        }
                    }
                }
            }
    }

    private fun updateReadStatusOnCloud(notifId: String, isRead: Boolean?) {
        val userId = auth.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ref = firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(notifId)

                if (isRead == null) {
                    ref.delete()
                } else {
                    ref.update("isRead", isRead)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}