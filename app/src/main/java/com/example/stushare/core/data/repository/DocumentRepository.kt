package com.example.stushare.core.data.repository

import android.net.Uri
import com.example.stushare.core.data.models.CommentEntity
import com.example.stushare.core.data.models.Document
import kotlinx.coroutines.flow.Flow

/**
 * Interface định nghĩa các hành vi thao tác dữ liệu.
 */
interface DocumentRepository {

    // ==========================================
    // 1. TRUY VẤN DỮ LIỆU (READ)
    // ==========================================

    fun getAllDocuments(): Flow<List<Document>>

    fun getDocumentById(documentId: String): Flow<Document>

    fun searchDocuments(query: String): Flow<List<Document>>

    fun getDocumentsByType(type: String): Flow<List<Document>>

    fun getDocumentsByAuthor(authorId: String): Flow<List<Document>>

    fun getBookmarkedDocuments(): Flow<List<Document>>


    // ==========================================
    // 2. ĐỒNG BỘ DỮ LIỆU (SYNC)
    // ==========================================

    suspend fun refreshDocuments(): Result<Unit>

    suspend fun refreshDocumentsIfStale()


    // ==========================================
    // 3. TÁC VỤ DỮ LIỆU (WRITE/UPDATE)
    // ==========================================

    suspend fun insertDocument(document: Document)

    suspend fun uploadDocument(
        title: String,
        description: String,
        fileUri: Uri,
        mimeType: String,
        coverUri: Uri?,
        author: String,
        type: String
    ): Result<String>

    suspend fun deleteDocument(documentId: String): Result<Unit>

    suspend fun incrementDownloadCount(documentId: String, authorId: String?, docTitle: String): Result<Unit>

    // 🟢 MỚI: Hàm báo cáo tài liệu
    suspend fun reportDocument(documentId: String, documentTitle: String, reason: String): Result<Unit>


    // ==========================================
    // 4. TƯƠNG TÁC NGƯỜI DÙNG (USER ACTIONS)
    // ==========================================

    // --- Bookmark (Lưu trữ) ---
    suspend fun isDocumentBookmarked(documentId: String): Result<Boolean>

    suspend fun toggleBookmark(documentId: String, isBookmarked: Boolean): Result<Unit>

    // --- Comment (Bình luận) ---
    fun getComments(documentId: String): Flow<List<CommentEntity>>

    suspend fun sendComment(documentId: String, content: String): Result<Unit>

    suspend fun deleteComment(documentId: String, commentId: String): Result<Unit>

    // --- Rating (Đánh giá) ---
    suspend fun rateDocument(documentId: String, rating: Int): Result<Unit>
}