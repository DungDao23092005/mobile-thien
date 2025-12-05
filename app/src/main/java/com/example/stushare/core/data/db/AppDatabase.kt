package com.example.stushare.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration // ⬅️ THÊM DÒNG NÀY
import androidx.sqlite.db.SupportSQLiteDatabase // ⬅️ THÊM DÒNG NÀY
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

    companion object {
        // Migration để thêm cột isAdmin vào UserEntity (Phiên bản 3 -> 4)
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Thêm cột 'isAdmin' vào bảng 'users'.
                // INTEGER NOT NULL DEFAULT 0: Room sử dụng INTEGER (0/1) để lưu Boolean (false/true)
                database.execSQL("ALTER TABLE users ADD COLUMN isAdmin INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}