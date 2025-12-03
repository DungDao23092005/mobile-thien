package com.example.stushare.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stushare.core.data.models.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    // 1. Lấy thông báo của user cụ thể (Sắp xếp mới nhất lên đầu)
    // 🟢 THÊM: WHERE userId = :userId để tránh lẫn lộn giữa các tài khoản
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotifications(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    // 🟢 SỬA: Đổi id từ Long -> String
    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    // 🟢 SỬA: Đổi id từ Long -> String
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    // 🟢 THÊM: Đánh dấu tất cả là đã đọc
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    // 🟢 SỬA: Đếm tin chưa đọc của user cụ thể
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>
}