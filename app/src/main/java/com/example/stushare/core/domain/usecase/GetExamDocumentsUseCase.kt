package com.example.stushare.core.domain.usecase

import com.example.stushare.core.data.models.Document
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.utils.AppConstants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExamDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(): Flow<List<Document>> {
        // CẢI TIẾN: Gọi trực tiếp Query lọc theo Type từ DAO (Nhanh hơn)
        return repository.getDocumentsByType(AppConstants.TYPE_EXAM_PREP)
    }
}