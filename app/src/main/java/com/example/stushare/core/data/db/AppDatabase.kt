package com.example.stushare.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.NotificationEntity
import com.example.stushare.core.data.models.UserEntity

@Database(
    entities = [
        Document::class,           // Bảng Tài liệu
        NotificationEntity::class, // Bảng Thông báo
        UserEntity::class          // Bảng Người dùng
    ],
    // 🔴 TĂNG VERSION TỪ 3 -> 4 ĐỂ CẬP NHẬT CỘT fileUrl
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userDao(): UserDao

}