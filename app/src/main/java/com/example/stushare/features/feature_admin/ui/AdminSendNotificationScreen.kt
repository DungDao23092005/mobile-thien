package com.example.stushare.features.feature_admin.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.stushare.R
import com.example.stushare.ui.theme.PrimaryGreen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSendNotificationScreen(
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isProcessing by viewModel.isProcessing.collectAsState()

    // State cho Form
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isSendToAll by remember { mutableStateOf(true) }
    var targetEmail by remember { mutableStateOf("") }

    // Lắng nghe kết quả
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (message.contains("Đã gửi")) {
                onBackClick() // Gửi xong thì quay về
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_send_notif_screen_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Nhập Tiêu đề
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.label_notif_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 2. Nhập Nội dung
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.label_notif_content)) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            HorizontalDivider()

            // 3. Tùy chọn người nhận
            Text("Đối tượng nhận:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            // Option: Gửi tất cả
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSendToAll = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = isSendToAll, onClick = { isSendToAll = true })
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Group, null, tint = PrimaryGreen)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.opt_send_all), color = MaterialTheme.colorScheme.onSurface)
            }

            // Option: Gửi cá nhân
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSendToAll = false }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = !isSendToAll, onClick = { isSendToAll = false })
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Email, null, tint = Color.Blue)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.opt_send_specific), color = MaterialTheme.colorScheme.onSurface)
            }

            // Input Email (Chỉ hiện khi chọn gửi cá nhân)
            AnimatedVisibility(visible = !isSendToAll) {
                OutlinedTextField(
                    value = targetEmail,
                    onValueChange = { targetEmail = it },
                    label = { Text(stringResource(R.string.hint_target_email)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // 4. Nút Gửi
            Button(
                onClick = {
                    viewModel.sendSystemNotification(title, content, isSendToAll, targetEmail)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.msg_sending_notif))
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_send_now), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}