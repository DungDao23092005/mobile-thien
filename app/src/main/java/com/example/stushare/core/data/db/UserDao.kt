package com.example.stushare.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stushare.core.data.models.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    // ĐÃ SỬA: Tăng LIMIT lên 20 để khớp với logic tải từ Firestore
    @Query("SELECT * FROM users ORDER BY contributionPoints DESC LIMIT 20")
    fun getTopUsers(): Flow<List<UserEntity>>

    // Thêm hoặc cập nhật thông tin người dùng (REPLACE là đúng để cập nhật điểm mới)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Lấy thông tin một người dùng cụ thể
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
}