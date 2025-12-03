package com.example.stushare.core.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log // 🟢 Thêm Log
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun downloadFile(url: String, fileName: String): Long {
        // 🟢 1. In ra Log để xem URL có đúng không
        Log.d("DOWNLOAD_DEBUG", "--------------------------------------")
        Log.d("DOWNLOAD_DEBUG", "Bắt đầu tải file: $fileName")
        Log.d("DOWNLOAD_DEBUG", "URL gốc: '$url'")

        if (url.isBlank() || !url.startsWith("http")) {
            Log.e("DOWNLOAD_DEBUG", "❌ LỖI: URL không hợp lệ (Rỗng hoặc không phải http)")
            Toast.makeText(context, "Lỗi: Link tải không hợp lệ", Toast.LENGTH_SHORT).show()
            return -1L
        }

        return try {
            val manager = context.getSystemService(DownloadManager::class.java)
            val uri = Uri.parse(url)

            val request = DownloadManager.Request(uri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle(fileName)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadId = manager.enqueue(request)

            // 🟢 2. Kiểm tra xem DownloadManager có nhận đơn không
            Log.d("DOWNLOAD_DEBUG", "✅ Đã gửi yêu cầu tải. ID: $downloadId")
            Toast.makeText(context, "Đang bắt đầu tải xuống...", Toast.LENGTH_SHORT).show()

            downloadId
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DOWNLOAD_DEBUG", "❌ CRASH: ${e.message}")
            Toast.makeText(context, "Lỗi tải xuống: ${e.message}", Toast.LENGTH_SHORT).show()
            -1L
        }
    }
}