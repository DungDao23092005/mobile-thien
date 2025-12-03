package com.example.stushare.features.feature_request.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.models.DocumentRequest
import com.example.stushare.core.data.repository.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update // ⭐️ QUAN TRỌNG: Import update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RequestListUiState(
    val requests: List<DocumentRequest> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class RequestListViewModel @Inject constructor(
    private val repository: RequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestListUiState())
    val uiState: StateFlow<RequestListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllRequests()
                .catch { e ->
                    // ⭐️ CẬP NHẬT AN TOÀN: LỖI
                    _uiState.update {
                        RequestListUiState(
                            isLoading = false,
                            errorMessage = "Không thể tải yêu cầu: ${e.message}"
                        )
                    }
                }
                .collect { requestsFromFirestore ->
                    // ⭐️ CẬP NHẬT AN TOÀN: THÀNH CÔNG
                    _uiState.update {
                        RequestListUiState(
                            requests = requestsFromFirestore,
                            isLoading = false
                        )
                    }
                }
        }
    }
}