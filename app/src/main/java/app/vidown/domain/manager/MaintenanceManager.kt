package app.vidown.domain.manager

import android.content.Context
import androidx.work.*
import app.vidown.data.worker.ExtractorUpdateWorker
import java.util.concurrent.TimeUnit

object MaintenanceManager {
    private const val EXTRACTOR_UPDATE_WORK_NAME = "extractor_auto_update"

    fun scheduleExtractorUpdates(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)

        if (!enabled) {
            workManager.cancelUniqueWork(EXTRACTOR_UPDATE_WORK_NAME)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ExtractorUpdateWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            EXTRACTOR_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
