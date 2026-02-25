package app.vidown.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.vidown.data.repository.YtDlpRepository
import app.vidown.domain.models.VideoInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    data object Idle : HomeUiState()
    data object Loading : HomeUiState()
    data class Success(val videoInfo: VideoInfo) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = YtDlpRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchVideoInfo(url: String) {
        if (url.isBlank()) return

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            repository.fetchVideoInfo(url)
                .onSuccess { info ->
                    _uiState.value = HomeUiState.Success(info)
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(error.localizedMessage ?: "Unknown error occurred")
                }
        }
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}
