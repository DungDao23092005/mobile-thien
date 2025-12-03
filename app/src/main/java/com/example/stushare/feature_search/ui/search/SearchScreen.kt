package com.example.stushare.features.feature_search.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource // ⭐️ QUAN TRỌNG: Import cái này
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stushare.R // ⭐️ QUAN TRỌNG: Import R của project
import com.example.stushare.features.feature_search.ui.components.SearchTagChip
import com.example.stushare.ui.theme.PrimaryGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onSearchSubmit: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val currentQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearchesState.collectAsStateWithLifecycle()

    val keyboardController = LocalSoftwareKeyboardController.current

    // List gợi ý (Đã dùng stringResource -> OK)
    val suggestions = listOf(
        stringResource(R.string.sugg_law),
        stringResource(R.string.sugg_se),
        stringResource(R.string.sugg_it),
        stringResource(R.string.sugg_exam)
    )

    // Màu động (Đã OK)
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { query ->
            onSearchSubmit(query)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
    ) {
        // 1. Header
        SearchHeader(
            query = currentQuery,
            onQueryChange = viewModel::onQueryChanged,
            onBackClick = onBackClick,
            onClearClick = { viewModel.onQueryChanged("") },
            onSearch = {
                keyboardController?.hide()
                if (currentQuery.isNotBlank()) {
                    viewModel.onSearchTriggered(currentQuery)
                }
            }
        )

        // 2. Nội dung chính
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(backgroundColor)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp)
            ) {
                // Section: Lịch sử tìm kiếm
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ⭐️ SỬA 1: Thay "Tìm kiếm gần đây" bằng Resource
                            Text(
                                text = stringResource(R.string.search_recent),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                            TextButton(onClick = { viewModel.clearRecentSearches() }) {
                                // ⭐️ SỬA 2: Thay "Xóa tất cả" bằng Resource
                                Text(
                                    text = stringResource(R.string.clear_all),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(recentSearches) { search ->
                        RecentSearchItem(
                            text = search,
                            onClick = {
                                viewModel.onQueryChanged(search)
                                viewModel.onSearchTriggered(search)
                            }
                        )
                    }

                    item {
                        Divider(
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }

                // Section: Gợi ý
                item {
                    // ⭐️ SỬA 3: Thay "Gợi ý cho bạn" bằng Resource
                    Text(
                        text = stringResource(R.string.search_suggestion),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        suggestions.forEach { suggestion ->
                            SearchTagChip(
                                text = suggestion,
                                onClick = {
                                    viewModel.onQueryChanged(suggestion)
                                    viewModel.onSearchTriggered(suggestion)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    onSearch: () -> Unit
) {
    val inputBackgroundColor = MaterialTheme.colorScheme.surface
    val inputTextColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(PrimaryGreen)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
    ) {
        // Hàng tiêu đề
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cancel), // Sửa description
                    tint = Color.White
                )
            }
            // ⭐️ SỬA 4: Thay tiêu đề "Tìm kiếm" bằng Resource
            Text(
                text = stringResource(R.string.search_title),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Thanh tìm kiếm
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                // ⭐️ SỬA 5: Thay "Nhập tên tài liệu..." bằng Resource
                Text(
                    text = stringResource(R.string.search_hint),
                    color = inputTextColor.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(CircleShape),
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = inputBackgroundColor,
                unfocusedContainerColor = inputBackgroundColor,
                disabledContainerColor = inputBackgroundColor,
                focusedTextColor = inputTextColor,
                unfocusedTextColor = inputTextColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PrimaryGreen
            ),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryGreen)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.delete),
                            tint = inputTextColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )
    }
}

// ... (Hàm RecentSearchItem giữ nguyên vì đã chuẩn) ...
@Composable
private fun RecentSearchItem(text: String, onClick: () -> Unit) {
    // ... code giữ nguyên ...
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp).rotate(-45f)
        )
    }
}