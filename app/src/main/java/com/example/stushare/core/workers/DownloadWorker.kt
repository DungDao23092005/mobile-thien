package com.example.stushare.core.workers

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File

class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val fileUrl = inputData.getString(KEY_FILE_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: "document"

        // Đảm bảo tên file có đuôi .pdf
        val finalFileName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"

        return try {
            // 1. Kiểm tra file đã tồn tại chưa để tránh tải trùng
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetFile = File(downloadDir, finalFileName)

            if (targetFile.exists()) {
                // Nếu file đã có, báo thành công ngay (hoặc bạn có thể báo lỗi tùy logic)
                return Result.success()
            }

            // 2. Tạo yêu cầu tải xuống
            val downloadManager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(fileUrl)

            val request = DownloadManager.Request(uri).apply {
                setTitle(fileName)
                setDescription("Đang tải tài liệu về máy...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, finalFileName)
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val downloadId = downloadManager.enqueue(request)

            val outputData = workDataOf("DOWNLOAD_ID" to downloadId)
            Result.success(outputData)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        const val KEY_FILE_URL = "key_file_url"
        const val KEY_FILE_NAME = "key_file_name"
    }
}