package com.example.stushare.core.data.repository

import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.models.UserEntity
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getTopUsers(): Flow<List<UserEntity>>
    fun getTopDocuments(): Flow<List<Document>>
    suspend fun updateUserContribution(userId: String, points: Int)
    suspend fun refreshLeaderboard()
    suspend fun syncMissingFields() 
}