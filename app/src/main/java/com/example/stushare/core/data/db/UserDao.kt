package com.example.stushare.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stushare.core.data.models.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Lấy Top 10 người dùng có điểm cao nhất để hiển thị Bảng xếp hạng
    @Query("SELECT * FROM users ORDER BY contributionPoints DESC LIMIT 10")
    fun getTopUsers(): Flow<List<UserEntity>>

    // Thêm hoặc cập nhật thông tin người dùng
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Lấy thông tin một người dùng cụ thể (để cộng điểm)
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

}