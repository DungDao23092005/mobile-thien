package com.example.stushare.features.feature_search.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stushare.core.data.repository.SettingsRepository
import com.example.stushare.core.data.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Đã bao gồm update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val recentSearchesState: StateFlow<List<String>> = settingsRepository.recentSearches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onQueryChanged(newQuery: String) {
        // ⭐️ CẬP NHẬT AN TOÀN
        _searchQuery.update { newQuery }
    }

    init {
        _searchQuery
            .debounce(500L)
            .filter { it.length > 2 }
            .distinctUntilChanged()
            .onEach { query ->
                onSearchTriggered(query)
            }
            .launchIn(viewModelScope)
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            settingsRepository.clearRecentSearches()
        }
    }

    fun onSearchTriggered(query: String) {
        viewModelScope.launch {
            try {
                try {
                    repository.refreshDocumentsIfStale()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                _navigationEvent.emit(query)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}