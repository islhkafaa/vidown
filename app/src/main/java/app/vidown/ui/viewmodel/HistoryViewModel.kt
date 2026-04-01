package app.vidown.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.vidown.data.local.AppDatabase
import app.vidown.data.local.HistoryEntity
import app.vidown.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedSource = MutableStateFlow("all_sources")
    val selectedSource: StateFlow<String> = _selectedSource

    private val settingsRepository = SettingsRepository(application)

    val historyState: StateFlow<List<HistoryEntity>> =
        combine(
            db.historyDao().getAllHistory(),
            _searchQuery,
            _selectedSource
        ) { history, query, source ->
            history.filter { record ->
                val matchesQuery = if (query.isBlank()) true
                else record.title.contains(query, ignoreCase = true)

                val recordSource = extractSource(record.url)
                val matchesSource = if (source == "all_sources") true
                else recordSource.equals(source, ignoreCase = true)

                matchesQuery && matchesSource
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private fun extractSource(url: String): String {
        return when {
            url.contains("youtube.com") || url.contains("youtu.be") -> "YouTube"
            url.contains("tiktok.com") -> "TikTok"
            url.contains("instagram.com") -> "Instagram"
            url.contains("facebook.com") || url.contains("fb.watch") -> "Facebook"
            url.contains("twitter.com") || url.contains("x.com") -> "Twitter"
            else -> "other_source"
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedSource(source: String) {
        _selectedSource.value = source
    }

    fun retryDownload(record: HistoryEntity) {
        viewModelScope.launch {
            val wifiOnly = settingsRepository.wifiOnlyFlow.first()
            val request = app.vidown.domain.models.DownloadRequest(
                url = record.url,
                title = record.title,
                thumbnailUrl = record.thumbnailUrl,
                formatId = record.formatId,
                totalBytes = record.totalBytes
            )
            app.vidown.data.repository.DownloadQueueRepository.enqueueDownload(
                getApplication<Application>().applicationContext,
                request,
                wifiOnly
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch { db.historyDao().clearHistory() }
    }

    fun deleteRecord(record: HistoryEntity, context: android.content.Context) {
        viewModelScope.launch {
            if (record.fileUri != null) {
                app.vidown.data.repository.MediaStoreManager.deleteFile(context, record.fileUri)
            }
            db.historyDao().deleteById(record.id)
        }
    }
}
