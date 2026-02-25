package app.vidown.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import app.vidown.data.local.AppDatabase
import app.vidown.data.local.HistoryEntity
import app.vidown.data.repository.DownloadQueueRepository
import app.vidown.data.repository.MediaStoreManager
import app.vidown.domain.models.DownloadStatus
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import android.webkit.MimeTypeMap

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress for active downloads"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundInfo(progress: Int, title: String): ForegroundInfo {
        val notificationId = id.hashCode()
        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Downloading: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .addAction(android.R.drawable.ic_delete, "Cancel", cancelIntent)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val url = inputData.getString(KEY_URL) ?: return@withContext Result.failure()
        val formatId = inputData.getString(KEY_FORMAT_ID) ?: return@withContext Result.failure()
        val requestIdStr = inputData.getString(KEY_REQUEST_ID) ?: return@withContext Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: "unknown_video"
        var totalBytes = inputData.getLong(KEY_TOTAL_BYTES, 0L)
        val thumbnailUrl = inputData.getString(KEY_THUMBNAIL)
        val sizeRegex = Regex("""of\s+~?\s*([0-9.]+)([a-zA-Z]+)""")

        val requestId = UUID.fromString(requestIdStr)
        val db = AppDatabase.getDatabase(applicationContext)

        try {
            setForeground(createForegroundInfo(0, title))

            val downloadDir = File(applicationContext.cacheDir, "vidown_temp")
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val safeTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val outputTemplate = File(downloadDir, "$safeTitle.%(ext)s").absolutePath

            val request = YoutubeDLRequest(url).apply {
                addOption("-f", formatId)
                addOption("-o", outputTemplate)
                addOption("--no-mtime")

                if (formatId == "bestaudio" || formatId.contains("m4a") || formatId.contains("mp3")) {
                     addOption("--extract-audio")
                     addOption("--audio-format", "m4a")
                }
            }

            DownloadQueueRepository.updateStatus(requestId, DownloadStatus.Downloading)

            var lastNotificationTime = 0L

            YoutubeDL.getInstance().execute(request, requestIdStr) { progress, etaInSeconds, line ->
                val match = sizeRegex.find(line)
                if (match != null) {
                    val size = match.groupValues[1].toDoubleOrNull() ?: 0.0
                    val unit = match.groupValues[2].lowercase()
                    val multiplier = when (unit) {
                        "ki", "kib", "kb", "k" -> 1024L
                        "mi", "mib", "mb", "m" -> 1024L * 1024L
                        "gi", "gib", "gb", "g" -> 1024L * 1024L * 1024L
                        else -> 1L
                    }
                    totalBytes = (size * multiplier).toLong()
                }

                DownloadQueueRepository.updateProgress(
                    id = requestId,
                    progress = progress,
                    downloadedBytes = (totalBytes * (progress / 100f)).toLong(),
                    totalBytes = totalBytes
                )

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastNotificationTime > 1000) {
                    notificationManager.notify(
                        id.hashCode(),
                        createForegroundInfo(progress.toInt(), title).notification
                    )
                    lastNotificationTime = currentTime
                }
            }

            val downloadedFile = downloadDir.listFiles()?.find { it.name.startsWith(safeTitle) }
                ?: throw Exception("Downloaded file not found in temp directory")

            val extension = downloadedFile.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"

            val isVideo = extension in listOf("mp4", "webm", "mkv", "avi")

            val success = MediaStoreManager.saveFile(
                context = applicationContext,
                tempFile = downloadedFile,
                title = safeTitle,
                mimeType = mimeType,
                isVideo = isVideo
            )

            if (downloadedFile.exists()) {
                downloadedFile.delete()
            }

            if (success) {
                DownloadQueueRepository.updateStatus(requestId, DownloadStatus.Success)
                db.historyDao().insertHistory(
                    HistoryEntity(
                        id = requestIdStr,
                        url = url,
                        title = safeTitle,
                        formatId = formatId,
                        thumbnailUrl = thumbnailUrl,
                        timestampMs = System.currentTimeMillis(),
                        totalBytes = totalBytes,
                        status = DownloadStatus.Success
                    )
                )
                Result.success()
            } else {
                throw Exception("Failed to save to MediaStore")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            DownloadQueueRepository.updateStatus(requestId, DownloadStatus.Failed)
            db.historyDao().insertHistory(
                HistoryEntity(
                    id = requestIdStr,
                    url = url,
                    title = title,
                    formatId = formatId,
                    thumbnailUrl = thumbnailUrl,
                    timestampMs = System.currentTimeMillis(),
                    totalBytes = totalBytes,
                    status = DownloadStatus.Failed
                )
            )
            Result.failure()
        }
    }

    companion object {
        const val KEY_URL = "key_url"
        const val KEY_FORMAT_ID = "key_format_id"
        const val KEY_REQUEST_ID = "key_request_id"
        const val KEY_TITLE = "key_title"
        const val KEY_THUMBNAIL = "key_thumbnail"
        const val KEY_TOTAL_BYTES = "key_total_bytes"
        const val CHANNEL_ID = "vidown_downloads"
    }
}
