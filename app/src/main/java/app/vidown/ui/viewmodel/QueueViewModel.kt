package app.vidown.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import app.vidown.data.repository.DownloadQueueRepository
import app.vidown.domain.models.DownloadRequest
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class QueueViewModel : ViewModel() {
    val downloadQueue: StateFlow<List<DownloadRequest>> = DownloadQueueRepository.downloadQueue

    fun pauseDownload(id: UUID) {
        DownloadQueueRepository.pauseDownload(id)
    }

    fun resumeDownload(id: UUID) {
        DownloadQueueRepository.resumeDownload(id)
    }

    fun removeDownload(context: Context, id: UUID) {
        DownloadQueueRepository.removeDownload(context, id)
    }
}
