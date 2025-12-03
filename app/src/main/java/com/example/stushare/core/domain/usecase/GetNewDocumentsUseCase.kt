package com.example.stushare.core.domain.usecase

import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNewDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(): Flow<List<Document>> {
        // Lấy danh sách tổng hợp từ Database
        return repository.getAllDocuments()
            .map { documents ->
                documents
                    // 1. Sắp xếp giảm dần theo thời gian (Mới nhất lên đầu)
                    .sortedByDescending { it.createdAt }
                    // 2. Chỉ lấy 10 tài liệu mới nhất (để hiển thị ở mục Hot)
                    .take(10)
            }
    }
}