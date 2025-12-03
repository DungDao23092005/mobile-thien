package com.example.stushare.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidFileOpener @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun openFile(url: String) {
        try {
            if (url.isBlank()) {
                Toast.makeText(context, "Link tài liệu bị lỗi", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri

            // Cờ này cần thiết vì chúng ta gọi Intent từ ngoài Activity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            // Tạo menu "Mở bằng..." để người dùng chọn ứng dụng
            val chooser = Intent.createChooser(intent, "Chọn ứng dụng để mở tài liệu")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Không tìm thấy ứng dụng hỗ trợ mở file này", Toast.LENGTH_LONG).show()
        }
    }
}