package com.example.stushare.features.auth.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.stushare.R
import com.example.stushare.core.navigation.NavRoute
import androidx.core.content.edit

@Composable
fun ManHinhChao(boDieuHuong: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(2000) // Chờ 2 giây

        val sharedPreferences = context.getSharedPreferences("CauHinhApp", Context.MODE_PRIVATE)
        val laLanDau = sharedPreferences.getBoolean("lanDauMoApp", true)

        if (laLanDau) {
            // LẦN ĐẦU: Lưu trạng thái và sang màn hình GIỚI THIỆU
            sharedPreferences.edit { putBoolean("lanDauMoApp", false) }

            boDieuHuong.navigate(NavRoute.Onboarding) {
                popUpTo(NavRoute.Intro) { inclusive = true }
            }
        } else {
            // LẦN SAU: Sang thẳng HOME để khách trải nghiệm ngay (Thay vì Login)
            boDieuHuong.navigate(NavRoute.Home) {
                popUpTo(NavRoute.Intro) { inclusive = true }
            }
        }
    }

    NenHinhSong {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(400.dp)
            )
        }
    }
}