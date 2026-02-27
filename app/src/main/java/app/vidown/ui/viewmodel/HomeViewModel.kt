package app.vidown.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.vidown.data.repository.YtDlpRepository
import app.vidown.domain.models.VideoInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import app.vidown.data.repository.DownloadQueueRepository
import app.vidown.data.repository.SettingsRepository
import app.vidown.domain.models.DownloadRequest
import app.vidown.domain.models.DownloadStatus
import app.vidown.domain.models.Format
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

sealed class HomeUiState {
    data object Idle : HomeUiState()
    data object Loading : HomeUiState()
    data class Success(val videoInfo: VideoInfo) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = YtDlpRepository(application.applicationContext)
    private val settingsRepository = SettingsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun fetchVideoInfo(url: String) {
        if (url.isBlank()) return

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            repository.fetchVideoInfo(url, allowPlaylist = true)
                .onSuccess { info ->
                    _uiState.value = HomeUiState.Success(info)
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(error.localizedMessage ?: "Unknown error occurred")
                }
        }
    }

    suspend fun getDefaultResolution(): String {
        return settingsRepository.defaultResolutionFlow.first()
    }

    fun startDownload(videoInfo: VideoInfo, format: Format) {
        val downloadFormatId = if (format.isVideo && format.acodec == "none") {
            "${format.formatId}+bestaudio"
        } else {
            format.formatId
        }

        val request = DownloadRequest(
            url = videoInfo.url,
            title = videoInfo.title,
            thumbnailUrl = videoInfo.thumbnailUrl,
            formatId = downloadFormatId,
            totalBytes = format.displaySize ?: 0L
        )

        DownloadQueueRepository.enqueueDownload(getApplication<Application>().applicationContext, request)
    }

    fun downloadPlaylist(playlistInfo: VideoInfo) {
        val entries = playlistInfo.entries ?: return
        val context = getApplication<Application>().applicationContext
        entries.forEach { entry ->
            val request = DownloadRequest(
                id = UUID.randomUUID(),
                url = entry.url,
                title = entry.title,
                thumbnailUrl = entry.thumbnailUrl,
                formatId = "best",
                totalBytes = 0L
            )
            DownloadQueueRepository.enqueueDownload(context, request)
        }
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}
