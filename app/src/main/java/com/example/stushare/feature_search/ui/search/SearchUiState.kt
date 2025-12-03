// File: features/feature_search/ui/search/SearchUiState.kt
package com.example.stushare.feature_search.ui.search

import com.example.stushare.core.data.models.Document

// Đây là định nghĩa DUY NHẤT
sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val results: List<Document>, val resultCount: Int) : SearchUiState
    data class Error(val message: String) : SearchUiState
    data object Empty : SearchUiState
    data object Initial : SearchUiState
}