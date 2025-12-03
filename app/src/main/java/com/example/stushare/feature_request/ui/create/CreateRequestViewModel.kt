package com.example.stushare.features.feature_request.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRequestViewModel @Inject constructor(
    private val repository: RequestRepository
) : ViewModel() {

    fun submitRequest(title: String, subject: String, description: String) {
        viewModelScope.launch {
            repository.createRequest(title, subject, description)
        }
    }
}