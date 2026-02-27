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
import app.vidown.data.util.VidownNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class Application : Application(), androidx.work.Configuration.Provider {

    override val workManagerConfiguration: androidx.work.Configuration
        get() {
            val settingsRepo = app.vidown.data.repository.SettingsRepository(this)
            val limit = runBlocking { settingsRepo.concurrentDownloadsFlow.firstOrNull() ?: 3 }

            return androidx.work.Configuration.Builder()
                .setExecutor(Executors.newFixedThreadPool(limit))
                .setMinimumLoggingLevel(Log.INFO)
                .build()
        }

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

        VidownNotificationManager.init(this)
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
