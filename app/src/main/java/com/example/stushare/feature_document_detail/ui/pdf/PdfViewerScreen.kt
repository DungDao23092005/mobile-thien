package com.example.stushare.features.feature_document_detail.ui.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.stushare.ui.theme.PrimaryGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    url: String,
    title: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State quản lý PdfRenderer và số trang
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Tải file và khởi tạo PdfRenderer
    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Giải mã URL và tải file về Cache
                val decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
                val connection = URL(decodedUrl).openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()

                // Lưu vào file tạm trong bộ nhớ cache của ứng dụng
                val file = File(context.cacheDir, "temp_pdf_${System.currentTimeMillis()}.pdf")
                file.outputStream().use { output ->
                    inputStream.copyTo(output)
                }

                // 2. Mở file bằng PdfRenderer
                val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)

                pdfRenderer = renderer
                pageCount = renderer.pageCount
                isLoading = false

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Không thể tải tài liệu: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    // Dọn dẹp bộ nhớ khi thoát màn hình
    DisposableEffect(Unit) {
        onDispose {
            try {
                pdfRenderer?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.LightGray) // Màu nền xám cho dễ nhìn trang giấy
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryGreen
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "Lỗi không xác định",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (pdfRenderer != null) {
                // Hiển thị danh sách các trang PDF
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp), // Khoảng cách giữa các trang
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(pageCount) { index ->
                        PdfPage(renderer = pdfRenderer!!, pageIndex = index)
                    }
                }
            }
        }
    }
}

@Composable
fun PdfPage(renderer: PdfRenderer, pageIndex: Int) {
    // Tính toán kích thước màn hình để render trang PDF cho vừa vặn
    val density = LocalDensity.current
    val screenWidth = with(density) {
        LocalContext.current.resources.displayMetrics.widthPixels
    }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Render trang PDF ra Bitmap (Ảnh)
    LaunchedEffect(pageIndex) {
        withContext(Dispatchers.IO) {
            // Đồng bộ hóa để tránh lỗi render cùng lúc nhiều trang
            synchronized(renderer) {
                try {
                    val page = renderer.openPage(pageIndex)

                    // Tính tỷ lệ để ảnh sắc nét
                    val width = screenWidth
                    val height = (screenWidth.toFloat() / page.width * page.height).toInt()

                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    page.close()
                    bitmap = bmp
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Page ${pageIndex + 1}",
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White) // Trang giấy màu trắng
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth
        )
    } else {
        // Placeholder khi đang load trang
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }
}