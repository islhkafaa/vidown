package app.vidown.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.vidown.data.repository.DownloadQueueRepository
import app.vidown.data.repository.SettingsRepository
import app.vidown.data.repository.YtDlpRepository
import app.vidown.domain.models.DownloadRequest
import app.vidown.domain.models.Format
import app.vidown.domain.models.VideoInfo
import java.util.UUID
import android.content.ClipboardManager
import android.util.Patterns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    private val _clipboardUrl = MutableStateFlow<String?>(null)
    val clipboardUrl: StateFlow<String?> = _clipboardUrl.asStateFlow()

    private var lastCheckedClip: String? = null

    fun fetchVideoInfo(url: String) {
        if (url.isBlank()) return

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            repository
                .fetchVideoInfo(url, allowPlaylist = true)
                .onSuccess { info -> _uiState.value = HomeUiState.Success(info) }
                .onFailure { error ->
                    _uiState.value =
                        HomeUiState.Error(error.localizedMessage ?: "Unknown error occurred")
                }
        }
    }

    fun startDownload(videoInfo: VideoInfo, format: Format) {
        viewModelScope.launch {
            val wifiOnly = settingsRepository.wifiOnlyFlow.first()
            val downloadFormatId =
                if (format.isVideo && format.acodec == "none") {
                    "${format.formatId}+bestaudio"
                } else {
                    format.formatId
                }

            val request =
                DownloadRequest(
                    url = videoInfo.displayUrl,
                    title = videoInfo.title,
                    thumbnailUrl = videoInfo.thumbnailUrl,
                    formatId = downloadFormatId,
                    totalBytes = format.displaySize
                )

            DownloadQueueRepository.enqueueDownload(
                getApplication<Application>().applicationContext,
                request,
                wifiOnly
            )
        }
    }

    fun downloadPlaylist(playlistInfo: VideoInfo) {
        viewModelScope.launch {
            val wifiOnly = settingsRepository.wifiOnlyFlow.first()
            val entries = playlistInfo.entries ?: return@launch
            val context = getApplication<Application>().applicationContext
            entries.forEach { entry ->
                val request =
                    DownloadRequest(
                        id = UUID.randomUUID(),
                        url = entry.displayUrl,
                        title = entry.title,
                        thumbnailUrl = entry.thumbnailUrl,
                        formatId = "best",
                        totalBytes = 0L
                    )
                DownloadQueueRepository.enqueueDownload(context, request, wifiOnly)
            }
        }
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }

    fun checkClipboard(clipboardManager: ClipboardManager) {
        val clipData = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text?.toString() ?: ""
            if (text != lastCheckedClip && isValidUrl(text)) {
                _clipboardUrl.value = text
                lastCheckedClip = text
            }
        }
    }

    fun clearClipboardSuggestion() {
        _clipboardUrl.value = null
    }

    private fun isValidUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches() &&
                (url.contains("youtube.com") || url.contains("youtu.be") ||
                        url.contains("instagram.com") || url.contains("facebook.com") ||
                        url.contains("tiktok.com") || url.contains("twitter.com") || url.contains("x.com"))
    }
}
