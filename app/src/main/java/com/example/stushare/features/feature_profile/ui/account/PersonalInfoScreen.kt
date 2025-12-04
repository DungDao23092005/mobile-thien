package com.example.stushare.features.feature_profile.ui.account

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.stushare.R
import com.example.stushare.features.feature_profile.ui.main.ProfileUiState
import com.example.stushare.features.feature_profile.ui.main.ProfileViewModel
import com.example.stushare.features.feature_profile.ui.model.UserProfile
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isUploadingAvatar by viewModel.isUploadingAvatar.collectAsStateWithLifecycle()

    // Local State
    var name by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    // Các chuỗi resource cần dùng trong logic (Toast)
    val errNameEmpty = stringResource(R.string.err_name_empty)

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Authenticated) {
            val profile = (uiState as ProfileUiState.Authenticated).profile
            name = profile.fullName
            major = profile.major
            bio = profile.bio
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadAvatar(uri)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                // [Thay đổi] Sử dụng stringResource cho Title
                title = { 
                    Text(
                        text = stringResource(R.string.p_info_title), 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp, 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PrimaryGreen)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is ProfileUiState.Unauthenticated -> {
                    // [Thay đổi] Sử dụng stringResource
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                        Text(stringResource(R.string.err_login_required)) 
                    }
                }
                is ProfileUiState.Authenticated -> {
                    PersonalInfoContent(
                        userProfile = state.profile,
                        nameState = name,
                        majorState = major,
                        bioState = bio,
                        onNameChange = { name = it },
                        onMajorChange = { major = it },
                        onBioChange = { bio = it },
                        isUploadingAvatar = isUploadingAvatar,
                        onAvatarClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onSaveClick = {
                            if (name.isNotBlank()) {
                                focusManager.clearFocus()
                                if (name != state.profile.fullName) {
                                    viewModel.updateUserName(name)
                                }
                                viewModel.updateExtendedInfo(major, bio)
                            } else {
                                // [Thay đổi] Sử dụng biến resource đã lấy
                                Toast.makeText(context, errNameEmpty, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoContent(
    userProfile: UserProfile,
    nameState: String,
    majorState: String,
    bioState: String,
    onNameChange: (String) -> Unit,
    onMajorChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    isUploadingAvatar: Boolean,
    onAvatarClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    // [Thay đổi] Tạo danh sách Majors bằng stringResource để hỗ trợ đa ngôn ngữ
    val majors = listOf(
        stringResource(R.string.major_it),
        stringResource(R.string.major_transport_eco),
        stringResource(R.string.major_electrical),
        stringResource(R.string.major_mechanical),
        stringResource(R.string.major_construction),
        stringResource(R.string.major_transport_eng),
        stringResource(R.string.major_environment),
        stringResource(R.string.major_other)
    )
    
    var expandedMajor by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                // 1. AVATAR
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = userProfile.avatarUrl ?: "https://ui-avatars.com/api/?name=${userProfile.fullName}&background=random",
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable(enabled = !isUploadingAvatar) { onAvatarClick() },
                        contentScale = ContentScale.Crop
                    )
                    if (isUploadingAvatar) {
                        CircularProgressIndicator(modifier = Modifier.size(100.dp), color = PrimaryGreen, strokeWidth = 3.dp)
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(32.dp).clip(CircleShape).background(PrimaryGreen).clickable { onAvatarClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        // [Thay đổi] Content description
                        Icon(Icons.Default.CameraAlt, stringResource(R.string.change_avatar), tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 2. EMAIL (READ-ONLY)
                OutlinedTextField(
                    value = userProfile.email,
                    onValueChange = {},
                    enabled = false,
                    // [Thay đổi] stringResource
                    label = { Text(stringResource(R.string.acc_sec_email)) },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. TÊN HIỂN THỊ
                OutlinedTextField(
                    value = nameState,
                    onValueChange = onNameChange,
                    // [Thay đổi] stringResource
                    label = { Text(stringResource(R.string.label_fullname)) },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, focusedLabelColor = PrimaryGreen, cursorColor = PrimaryGreen)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🟢 4. CHUYÊN NGÀNH (DROPDOWN)
                ExposedDropdownMenuBox(
                    expanded = expandedMajor,
                    onExpandedChange = { expandedMajor = !expandedMajor }
                ) {
                    OutlinedTextField(
                        value = majorState,
                        onValueChange = {},
                        readOnly = true,
                        // [Thay đổi] stringResource
                        label = { Text(stringResource(R.string.label_major)) },
                        leadingIcon = { Icon(Icons.Default.School, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMajor) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, focusedLabelColor = PrimaryGreen)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMajor,
                        onDismissRequest = { expandedMajor = false }
                    ) {
                        majors.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    onMajorChange(selectionOption)
                                    expandedMajor = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🟢 5. BIO / GIỚI THIỆU
                OutlinedTextField(
                    value = bioState,
                    onValueChange = onBioChange,
                    // [Thay đổi] stringResource
                    label = { Text(stringResource(R.string.label_bio)) },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    // [Thay đổi] stringResource
                    placeholder = { Text(stringResource(R.string.hint_bio)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen, focusedLabelColor = PrimaryGreen)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 6. NÚT LƯU
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Save, null, Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    // [Thay đổi] stringResource
                    Text(stringResource(R.string.p_info_save), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}