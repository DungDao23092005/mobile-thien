package com.example.stushare.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.stushare.core.data.models.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    // 1. Lấy tất cả tài liệu (Dùng cho Home/List)
    // Flow giúp UI tự động render lại khi bảng thay đổi
    @Query("SELECT * FROM documents ORDER BY createdAt DESC") // Nên thêm sắp xếp theo thời gian
    fun getAllDocuments(): Flow<List<Document>>

    // 2. Lấy chi tiết tài liệu
    @Query("SELECT * FROM documents WHERE id = :documentId")
    fun getDocumentById(documentId: String): Flow<Document>

    // 3. Insert hoặc Update (Nếu trùng ID thì ghi đè)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDocuments(documents: List<Document>)

    // 4. Tìm kiếm (CẢI TIẾN: Chuyển sang Flow để search cũng realtime)
    // Lưu ý: COLLATE NOACCENT có thể cần cài đặt thêm tùy phiên bản Android/Room.
    // Nếu lỗi crash, hãy bỏ "COLLATE NOACCENT" đi.
    @Query("SELECT * FROM documents WHERE normalizedTitle LIKE '%' || :query || '%'")
    fun searchDocuments(query: String): Flow<List<Document>>

    // 5. Lấy theo loại (Flow)
    @Query("SELECT * FROM documents WHERE type = :type")
    fun getDocumentsByType(type: String): Flow<List<Document>>

    // 6. Lấy top tài liệu (Flow)
    @Query("SELECT * FROM documents ORDER BY downloads DESC LIMIT 10")
    fun getTopDocuments(): Flow<List<Document>>

    // 7. Xóa toàn bộ
    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()

    // 8. Transaction làm mới dữ liệu từ API về Local
    @Transaction
    suspend fun replaceAllDocuments(documents: List<Document>) {
        deleteAllDocuments()
        insertAllDocuments(documents)
    }

    // ⭐️ QUAN TRỌNG: Sửa lại kiểu dữ liệu ID thành String (khớp với model Document thường dùng)
    // Nếu trong Model Document của bạn id là Long, hãy đổi lại thành Long.
    // Nhưng nếu id từ API về là chuỗi ký tự, PHẢI LÀ STRING.
    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)
}