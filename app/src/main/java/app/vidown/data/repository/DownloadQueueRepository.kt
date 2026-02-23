package app.vidown.data.repository

import app.vidown.domain.models.DownloadRequest
import app.vidown.domain.models.DownloadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object DownloadQueueRepository {
    private val _downloadQueue = MutableStateFlow<List<DownloadRequest>>(emptyList())
    val downloadQueue: StateFlow<List<DownloadRequest>> = _downloadQueue.asStateFlow()

    fun addDownload(request: DownloadRequest) {
        _downloadQueue.update { currentList ->
            val mutableList = currentList.toMutableList()
            if (mutableList.none { it.id == request.id }) {
                mutableList.add(request)
            }
            mutableList
        }
    }

    fun updateProgress(id: UUID, progress: Float, downloadedBytes: Long, totalBytes: Long) {
        _downloadQueue.update { currentList ->
            currentList.map { request ->
                if (request.id == id) {
                    request.copy(
                        progress = progress,
                        downloadedBytes = downloadedBytes,
                        totalBytes = totalBytes,
                        status = DownloadStatus.Downloading
                    )
                } else request
            }
        }
    }

    fun updateStatus(id: UUID, status: DownloadStatus) {
        _downloadQueue.update { currentList ->
            currentList.map { request ->
                if (request.id == id) {
                    request.copy(status = status)
                } else request
            }
        }
    }

    fun getDownload(id: UUID): DownloadRequest? {
        return _downloadQueue.value.find { it.id == id }
    }
}
