package com.example.stushare.features.feature_admin.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stushare.core.data.models.Report
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportScreen(
    onBackClick: () -> Unit,
    onDocumentClick: (String) -> Unit, // Callback mở tài liệu
    viewModel: AdminViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState() // 🟢 Dùng đúng biến isProcessing
    val context = LocalContext.current

    // Lắng nghe Toast thông báo kết quả
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý vi phạm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF0F0), // Màu đỏ nhạt
                    titleContentColor = Color.Red
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (reports.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Done, null, tint = Color.Green, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Sạch bóng! Không có báo cáo nào.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reports) { report ->
                        ReportItem(
                            report = report,
                            onDelete = { viewModel.deleteDocument(report.documentId, report.id) },
                            onDismiss = { viewModel.dismissReport(report.id) },
                            onViewDocument = { onDocumentClick(report.documentId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItem(
    report: Report,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    onViewDocument: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Lý do
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(report.reason, fontWeight = FontWeight.Bold, color = Color.Red)
            }
            Spacer(Modifier.height(12.dp))

            // Content
            Text("Tài liệu: ${report.documentTitle.ifBlank { "ID: ${report.documentId}" }}", fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Người báo cáo: ${report.reporterEmail}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            val date = try { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(report.timestamp) } catch (e: Exception) { "" }
            Text("Ngày: $date", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onViewDocument) {
                    Icon(Icons.Default.Visibility, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Xem thử")
                }
                Row {
                    OutlinedButton(onClick = onDismiss, contentPadding = PaddingValues(horizontal = 12.dp)) {
                        Text("Bỏ qua", color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Xóa")
                    }
                }
            }
        }
    }
}