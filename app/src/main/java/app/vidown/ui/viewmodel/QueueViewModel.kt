package app.vidown.ui.viewmodel

import androidx.lifecycle.ViewModel
import app.vidown.data.repository.DownloadQueueRepository
import app.vidown.domain.models.DownloadRequest
import kotlinx.coroutines.flow.StateFlow

class QueueViewModel : ViewModel() {
    val downloadQueue: StateFlow<List<DownloadRequest>> = DownloadQueueRepository.downloadQueue
}
