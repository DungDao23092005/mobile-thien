package com.example.stushare.core.utils // Hoặc package utils của bạn

import android.content.Context
import android.content.Intent
import com.example.stushare.MainActivity

fun Context.restartApp() {
    val intent = Intent(this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    startActivity(intent)
    // Kết thúc process hiện tại để đảm bảo sạch sẽ
    Runtime.getRuntime().exit(0)
}