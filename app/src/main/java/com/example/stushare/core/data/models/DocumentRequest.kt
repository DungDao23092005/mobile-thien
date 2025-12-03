package com.example.stushare.core.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DocumentRequest(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val authorName: String = "",

    // 🟢 THÊM: ID và Avatar người tạo để hiển thị trong đoạn chat
    val authorId: String = "",
    val authorAvatar: String? = null,

    val subject: String = "",
    val description: String = "",

    @ServerTimestamp
    val createdAt: Date? = null
) {
    constructor() : this("", "", "", "", null, "", "", null)
}