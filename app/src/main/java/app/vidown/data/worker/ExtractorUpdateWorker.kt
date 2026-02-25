package app.vidown.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.vidown.data.repository.YtDlpRepository
import com.yausername.youtubedl_android.YoutubeDL

class ExtractorUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ExtractorUpdateWorker", "Starting periodic yt-dlp extractor update check...")
        val repository = YtDlpRepository(applicationContext)

        return try {
            val result = repository.updateYtDlp()
            if (result.isSuccess) {
                val status = result.getOrNull()
                Log.d("ExtractorUpdateWorker", "Update finished with status: $status")
                Result.success()
            } else {
                Log.e("ExtractorUpdateWorker", "Update failed: ${result.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("ExtractorUpdateWorker", "Exception during update", e)
            Result.retry()
        }
    }
}
