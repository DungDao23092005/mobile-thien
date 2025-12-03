package com.example.stushare.core.navigation

import kotlinx.serialization.Serializable

sealed interface NavRoute {
    // --- AUTHENTICATION ---
    @Serializable data object Intro : NavRoute
    @Serializable data object Onboarding : NavRoute
    @Serializable data object Login : NavRoute
    @Serializable data object Register : NavRoute
    @Serializable data object ForgotPassword : NavRoute
    @Serializable data object LoginSMS : NavRoute
    @Serializable data class VerifyOTP(val verificationId: String) : NavRoute

    // --- MAIN FEATURES ---
    @Serializable data object Profile : NavRoute
    @Serializable data object Home : NavRoute
    @Serializable data object Search : NavRoute
    @Serializable data object RequestList : NavRoute
    @Serializable data object CreateRequest : NavRoute
    @Serializable data class DocumentDetail(val documentId: String) : NavRoute
    @Serializable data class ViewAll(val category: String) : NavRoute
    @Serializable data class SearchResult(val query: String) : NavRoute
    @Serializable data object Upload : NavRoute
    @Serializable data object Notification : NavRoute
    @Serializable data object Leaderboard : NavRoute
    @Serializable data class RequestDetail(val requestId: String) : NavRoute
    @Serializable data class PdfViewer(val url: String, val title: String) : NavRoute

    // --- ADMIN FEATURES ---
    // 🟢 Dashboard tổng quan (Thống kê)
    @Serializable data object AdminDashboard : NavRoute

    // 🟢 Danh sách báo cáo vi phạm (Cần thêm cái này để click vào nút "Duyệt tài liệu")
    @Serializable data object AdminReports : NavRoute

    // --- SETTINGS ---
    @Serializable data object Settings : NavRoute
    @Serializable data object AccountSecurity : NavRoute
    @Serializable data object ChangePassword : NavRoute
    @Serializable data object NotificationSettings : NavRoute
    @Serializable data object AppearanceSettings : NavRoute
    @Serializable data object AboutApp : NavRoute
    @Serializable data object ContactSupport : NavRoute
    @Serializable data object ReportViolation : NavRoute
    @Serializable data object SwitchAccount : NavRoute
    @Serializable data object PersonalInfo : NavRoute
}