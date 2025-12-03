package com.example.stushare.features.auth.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.stushare.R
import com.example.stushare.core.navigation.NavRoute

data class DuLieuGioiThieu(val tieuDe: String, val moTa: String, val hinhAnh: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ManHinhGioiThieu(boDieuHuong: NavController) {
    val danhSachTrang = listOf(
        DuLieuGioiThieu(
            "Tìm kiếm thông minh",
            "Truy cập kho tài liệu khổng lồ từ mọi khoa và môn học tại UTH. Tìm kiếm đề thi, slide bài giảng nhanh chóng.",
            R.drawable.intro1
        ),
        DuLieuGioiThieu(
            "Chia sẻ tri thức",
            "Trở thành một phần của cộng đồng sinh viên UTH. Đăng tải tài liệu, giúp đỡ bạn bè và tích lũy điểm.",
            R.drawable.intro2
        ),
        DuLieuGioiThieu(
            "Ôn tập hiệu quả",
            "Tất cả tài liệu bạn cần cho các kỳ thi đều ở đây. Học tập thông minh hơn và sẵn sàng cho mọi thử thách.",
            R.drawable.intro3
        )
    )

    val trangThaiPager = rememberPagerState(pageCount = { danhSachTrang.size })
    val phamViCoroutine = rememberCoroutineScope()

    // HÀM MỚI: Chuyển sang Trang chủ
    fun denManHinhChinh() {
        boDieuHuong.navigate(NavRoute.Home) {
            popUpTo(NavRoute.Onboarding) { inclusive = true }
            popUpTo(NavRoute.Intro) { inclusive = true }
        }
    }

    NenHinhSong {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 50.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = trangThaiPager,
                modifier = Modifier.weight(1f)
            ) { viTri ->
                NoiDungTrang(danhSachTrang[viTri])
            }

            Row(
                Modifier
                    .height(30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(danhSachTrang.size) { iteration ->
                    val mauSac = if (trangThaiPager.currentPage == iteration) MauXanhSong else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(mauSac)
                            .size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (trangThaiPager.currentPage < danhSachTrang.size - 1) {
                        phamViCoroutine.launch {
                            trangThaiPager.animateScrollToPage(trangThaiPager.currentPage + 1)
                        }
                    } else {
                        denManHinhChinh() // Gọi hàm vào trang chủ
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MauXanhSong),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = if (trangThaiPager.currentPage == danhSachTrang.size - 1) "Bắt đầu ngay" else "Tiếp Tục",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = { denManHinhChinh() }) {
                Text("Bỏ qua", color = Color.Gray)
            }
        }
    }
}

@Composable
fun NoiDungTrang(duLieu: DuLieuGioiThieu) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = duLieu.hinhAnh),
            contentDescription = null,
            modifier = Modifier.size(280.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = duLieu.tieuDe,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = duLieu.moTa,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            lineHeight = 24.sp
        )
    }
}