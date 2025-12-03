package com.example.stushare.core.utils

import java.text.Normalizer
import java.util.regex.Pattern
object AppConstants {

    // --- Document Types (Sử dụng trong Database/API) ---
    const val TYPE_BOOK = "Sách"
    const val TYPE_EXAM_PREP = "exam_review"

    // --- Navigation Categories (Sử dụng trong UI/Navigation) ---
    const val CATEGORY_NEW_UPLOADS = "new_uploads"
    const val CATEGORY_EXAM_PREP = "exam_prep"

    // (Sau này bạn có thể thêm các hằng số khác ở đây)
}
fun String.removeAccents(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(temp).replaceAll("")
        .replace('đ', 'd').replace('Đ', 'd')
        .lowercase()
        .trim()
        .replace(" ", "")
}