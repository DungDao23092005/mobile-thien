package com.example.stushare.core.data.models

data class Report(
    val id: String = "",
    val documentId: String = "",
    val documentTitle: String = "", // 🟢 Hiển thị tên cho dễ nhìn
    val reason: String = "",
    val reporterId: String = "",
    val reporterEmail: String = "", // 🟢 Biết ai báo cáo
    val timestamp: Long = 0,
    val status: String = "pending" // pending, resolved, dismissed
)