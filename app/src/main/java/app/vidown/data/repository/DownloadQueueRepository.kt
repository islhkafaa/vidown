package app.vidown.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.vidown.data.worker.DownloadWorker
import app.vidown.domain.models.DownloadRequest
import app.vidown.domain.models.DownloadStatus
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

  fun enqueueDownload(context: Context, request: DownloadRequest) {
    addDownload(request)

    val inputData =
            Data.Builder()
                    .putString(DownloadWorker.KEY_URL, request.url)
                    .putString(DownloadWorker.KEY_FORMAT_ID, request.formatId)
                    .putString(DownloadWorker.KEY_REQUEST_ID, request.id.toString())
                    .putString(DownloadWorker.KEY_TITLE, request.title)
                    .putString(DownloadWorker.KEY_THUMBNAIL, request.thumbnailUrl)
                    .putLong(DownloadWorker.KEY_TOTAL_BYTES, request.totalBytes)
                    .build()

    val workRequest =
            OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(inputData)
                    .addTag(request.id.toString())
                    .build()

    WorkManager.getInstance(context).enqueue(workRequest)
  }

  fun updateProgress(
          id: UUID,
          progress: Float,
          downloadedBytes: Long,
          totalBytes: Long,
          speed: String? = null,
          eta: String? = null
  ) {
    _downloadQueue.update { currentList ->
      currentList.map { request ->
        if (request.id == id) {
          request.copy(
                  progress = progress,
                  downloadedBytes = downloadedBytes,
                  totalBytes = totalBytes,
                  speed = speed,
                  eta = eta,
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

  fun pauseDownload(id: UUID) {
    updateStatus(id, DownloadStatus.Paused)
  }

  fun resumeDownload(id: UUID) {
    updateStatus(id, DownloadStatus.Pending)
  }

  fun removeDownload(context: Context, id: UUID) {
    _downloadQueue.update { currentList -> currentList.filterNot { it.id == id } }
    WorkManager.getInstance(context).cancelAllWorkByTag(id.toString())
  }

  fun getDownload(id: UUID): DownloadRequest? {
    return _downloadQueue.value.find { it.id == id }
  }
}
