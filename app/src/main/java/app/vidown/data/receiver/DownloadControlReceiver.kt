package app.vidown.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.vidown.data.repository.DownloadQueueRepository
import app.vidown.data.worker.DownloadWorker
import java.util.UUID

class DownloadControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestIdStr = intent.getStringExtra(DownloadWorker.KEY_REQUEST_ID) ?: return
        val requestId = UUID.fromString(requestIdStr)

        when (intent.action) {
            DownloadWorker.ACTION_PAUSE -> {
                DownloadQueueRepository.pauseDownload(requestId)
            }
            DownloadWorker.ACTION_RESUME -> {
                DownloadQueueRepository.resumeDownload(requestId)
            }
        }
    }
}
