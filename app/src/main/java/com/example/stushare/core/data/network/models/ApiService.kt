package com.example.stushare.core.data.network.models

// ⭐️ XÓA: import com.example.stushare.core.data.models.DocumentRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * ApiService - Giao tiếp với backend hoặc mock server (My JSON Server)
 * Mục đích: Lấy tài liệu, tìm kiếm, và gửi yêu cầu tài liệu.
 */
interface ApiService {

    // =======================
    // 📚 TÀI LIỆU (Documents)
    // =======================

    /**
     * Lấy danh sách tất cả tài liệu.
     */
    @GET("documents")
    suspend fun getAllDocuments(): List<DocumentDto>

    /**
     * Tìm kiếm tài liệu theo tiêu đề.
     *
     * ⚠️ Lưu ý: My JSON Server không hỗ trợ tốt Unicode có dấu.
     * Vì vậy, nếu bạn dùng tiếng Việt, hãy lọc tại client trong ViewModel.
     */
    @GET("documents")
    suspend fun searchDocuments(@Query("title_like") query: String): List<DocumentDto>


    // =======================
    // 📬 YÊU CẦU (Requests)
    // =======================
    // ⭐️ CÁC HÀM CŨ ĐÃ BỊ XÓA ⭐️
}