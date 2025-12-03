package com.example.stushare.features.feature_leaderboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.UserEntity
import com.example.stushare.core.data.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repository: LeaderboardRepository
) : ViewModel() {

    // Danh sách Top Users (Tự động cập nhật từ Room)
    val topUsers: StateFlow<List<UserEntity>> = repository.getTopUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Danh sách Top Documents (Tự động cập nhật từ Room)
    val topDocuments: StateFlow<List<Document>> = repository.getTopDocuments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Tự động tải dữ liệu mới nhất từ Firestore khi mở màn hình
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.refreshLeaderboard()
        }
    }
}