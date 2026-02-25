package app.vidown

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.vidown.data.worker.ExtractorUpdateWorker
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                YoutubeDL.getInstance().init(this@Application)
                FFmpeg.getInstance().init(this@Application)
                Log.d("VidownApp", "YoutubeDL and FFmpeg initialized successfully")
            } catch (e: Exception) {
                Log.e("VidownApp", "Failed to initialize YoutubeDL or FFmpeg", e)
            }
        }

        setupPeriodicExtractorUpdate()
    }

    private fun setupPeriodicExtractorUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateRequest = PeriodicWorkRequestBuilder<ExtractorUpdateWorker>(
            repeatInterval = 3,
            repeatIntervalTimeUnit = java.util.concurrent.TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PeriodicExtractorUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest
        )
    }
}
