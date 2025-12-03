// File: core/data/models/DataFailureException.kt

package com.example.stushare.core.data.models

import java.io.IOException

/**
 * Sealed class đại diện cho các loại lỗi mà Data Layer có thể trả về.
 * Điều này giúp ViewModel không cần phải biết về Retrofit hay IOException.
 */
sealed class DataFailureException(message: String? = null, cause: Throwable? = null) :
    IOException(message, cause) {

    // Lỗi xảy ra khi không có kết nối Internet
    data object NetworkError : DataFailureException("Không có kết nối mạng. Vui lòng thử lại.")

    // Lỗi từ phía API (4xx, 5xx)
    data class ApiError(val code: Int) : DataFailureException("Lỗi máy chủ với mã $code.")

    // Lỗi không xác định khác
    data class UnknownError(override val message: String? = "Đã xảy ra lỗi không xác định.") :
        DataFailureException(message)
}