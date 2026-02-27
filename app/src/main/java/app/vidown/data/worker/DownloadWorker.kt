package app.vidown.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.vidown.data.local.AppDatabase
import app.vidown.data.local.HistoryEntity
import app.vidown.data.repository.DownloadQueueRepository
import app.vidown.data.repository.MediaStoreManager
import app.vidown.domain.models.DownloadStatus
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class DownloadWorker(appContext: Context, workerParams: WorkerParameters) :
        CoroutineWorker(appContext, workerParams) {

  private val notificationManager =
          appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
              NotificationChannel(CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW)
                      .apply { description = "Shows progress for active downloads" }
      notificationManager.createNotificationChannel(channel)
    }
  }

  private fun createForegroundInfo(
          progress: Int,
          title: String,
          speed: String? = null,
          eta: String? = null
  ): ForegroundInfo {
    val notificationId = id.hashCode()
    val requestIdStr = inputData.getString(KEY_REQUEST_ID) ?: ""
    val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

    val currentStatus = DownloadQueueRepository.getDownload(UUID.fromString(requestIdStr))?.status

    val pauseResumeAction =
            if (currentStatus == DownloadStatus.Paused) {
              val resumeIntent =
                      Intent(
                                      applicationContext,
                                      app.vidown.data.receiver.DownloadControlReceiver::class.java
                              )
                              .apply {
                                action = ACTION_RESUME
                                putExtra(KEY_REQUEST_ID, requestIdStr)
                              }
              val resumePendingIntent =
                      PendingIntent.getBroadcast(
                              applicationContext,
                              id.hashCode() + 2,
                              resumeIntent,
                              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                      )
              NotificationCompat.Action.Builder(
                              android.R.drawable.ic_media_play,
                              "Resume",
                              resumePendingIntent
                      )
                      .build()
            } else {
              val pauseIntent =
                      Intent(
                                      applicationContext,
                                      app.vidown.data.receiver.DownloadControlReceiver::class.java
                              )
                              .apply {
                                action = ACTION_PAUSE
                                putExtra(KEY_REQUEST_ID, requestIdStr)
                              }
              val pausePendingIntent =
                      PendingIntent.getBroadcast(
                              applicationContext,
                              id.hashCode() + 2,
                              pauseIntent,
                              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                      )
              NotificationCompat.Action.Builder(
                              android.R.drawable.ic_media_pause,
                              "Pause",
                              pausePendingIntent
                      )
                      .build()
            }

    val progressText =
            if (speed != null && eta != null) {
              "Progress: $progress% • $speed • ETA: $eta"
            } else {
              "Progress: $progress%"
            }

    val notification =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setContentTitle(
                            if (currentStatus == DownloadStatus.Paused) "Paused" else "Downloading"
                    )
                    .setContentText("$title ($progress%)")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("$title\n$progressText"))
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOngoing(true)
                    .setProgress(100, progress, progress < 0)
                    .addAction(pauseResumeAction)
                    .addAction(android.R.drawable.ic_delete, "Cancel", cancelIntent)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .build()

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    } else {
      ForegroundInfo(notificationId, notification)
    }
  }

  override suspend fun doWork(): Result =
          withContext(Dispatchers.IO) {
            val url = inputData.getString(KEY_URL) ?: return@withContext Result.failure()
            val formatId = inputData.getString(KEY_FORMAT_ID) ?: return@withContext Result.failure()
            val requestIdStr =
                    inputData.getString(KEY_REQUEST_ID) ?: return@withContext Result.failure()
            val title = inputData.getString(KEY_TITLE) ?: "unknown_video"
            var totalBytes = inputData.getLong(KEY_TOTAL_BYTES, 0L)
            val thumbnailUrl = inputData.getString(KEY_THUMBNAIL)
            val sizeRegex = Regex("""of\s+~?\s*([0-9.]+)([a-zA-Z]+)""")
            val speedRegex = Regex("""at\s+([0-9.]+)([a-zA-Z/s]+)""")
            val etaRegex = Regex("""ETA\s+([0-9:]+)""")

            val requestId = UUID.fromString(requestIdStr)
            val db = AppDatabase.getDatabase(applicationContext)

            try {
              com.yausername.youtubedl_android.YoutubeDL.getInstance().init(applicationContext)
              com.yausername.ffmpeg.FFmpeg.getInstance().init(applicationContext)

              setForeground(createForegroundInfo(0, title))

              val downloadDir = File(applicationContext.cacheDir, "vidown_temp")
              if (!downloadDir.exists()) downloadDir.mkdirs()

              val safeTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "_")
              val outputTemplate = File(downloadDir, "$safeTitle.%(ext)s").absolutePath

              val request =
                      YoutubeDLRequest(url).apply {
                        addOption("-f", formatId)
                        addOption("-o", outputTemplate)
                        addOption("--no-mtime")
                        addOption("--no-playlist")

                        if (formatId == "bestaudio" ||
                                        formatId.contains("m4a") ||
                                        formatId.contains("mp3")
                        ) {
                          addOption("--extract-audio")
                          addOption("--audio-format", "m4a")
                        }
                      }

              DownloadQueueRepository.updateStatus(requestId, DownloadStatus.Downloading)

              var lastNotificationTime = 0L

              YoutubeDL.getInstance().execute(request, requestIdStr) { progress, _, line ->
                if (isStopped) return@execute

                val match = sizeRegex.find(line)
                if (match != null) {
                  val size = match.groupValues[1].toDoubleOrNull() ?: 0.0
                  val unit = match.groupValues[2].lowercase()
                  val multiplier =
                          when (unit) {
                            "ki", "kib", "kb", "k" -> 1024L
                            "mi", "mib", "mb", "m" -> 1024L * 1024L
                            "gi", "gib", "gb", "g" -> 1024L * 1024L * 1024L
                            else -> 1L
                          }
                  totalBytes = (size * multiplier).toLong()
                }

                val speedMatch = speedRegex.find(line)
                val speed = speedMatch?.let { "${it.groupValues[1]} ${it.groupValues[2]}" }

                val etaMatch = etaRegex.find(line)
                val eta = etaMatch?.groupValues?.get(1)

                val currentDownloadStatus = DownloadQueueRepository.getDownload(requestId)?.status
                if (currentDownloadStatus == DownloadStatus.Paused) {
                  return@execute
                }

                DownloadQueueRepository.updateProgress(
                        id = requestId,
                        progress = progress,
                        downloadedBytes = (totalBytes * (progress / 100f)).toLong(),
                        totalBytes = totalBytes,
                        speed = speed,
                        eta = eta
                )

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastNotificationTime > 1000) {
                  notificationManager.notify(
                          id.hashCode(),
                          createForegroundInfo(progress.toInt(), title, speed, eta).notification
                  )
                  lastNotificationTime = currentTime
                }
              }

              if (isStopped ||
                              DownloadQueueRepository.getDownload(requestId)?.status ==
                                      DownloadStatus.Paused
              ) {
                return@withContext Result.retry()
              }

              val downloadedFile =
                      downloadDir.listFiles()?.find { it.name.startsWith(safeTitle) }
                              ?: throw Exception("Downloaded file not found in temp directory")

              val extension = downloadedFile.extension.lowercase()
              val mimeType =
                      MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                              ?: "application/octet-stream"

              val isVideo = extension in listOf("mp4", "webm", "mkv", "avi")

              val settingsRepository =
                      app.vidown.data.repository.SettingsRepository(applicationContext)
              val customUri = settingsRepository.downloadUriFlow.firstOrNull()

              val savedUriString =
                      MediaStoreManager.saveFile(
                              context = applicationContext,
                              tempFile = downloadedFile,
                              title = safeTitle,
                              mimeType = mimeType,
                              isVideo = isVideo,
                              customDirUri = customUri
                      )

              if (downloadedFile.exists()) {
                downloadedFile.delete()
              }

              if (savedUriString != null) {
                DownloadQueueRepository.updateStatus(requestId, DownloadStatus.Success)
                db.historyDao()
                        .insertHistory(
                                HistoryEntity(
                                        id = requestIdStr,
                                        url = url,
                                        title = safeTitle,
                                        formatId = formatId,
                                        thumbnailUrl = thumbnailUrl,
                                        timestampMs = System.currentTimeMillis(),
                                        totalBytes = totalBytes,
                                        status = DownloadStatus.Success,
                                        fileUri = savedUriString
                                )
                        )

                showSuccessNotification(safeTitle, savedUriString, mimeType)
                Result.success()
              } else {
                throw Exception("Failed to save to MediaStore")
              }
            } catch (e: Exception) {
              if (isStopped ||
                              DownloadQueueRepository.getDownload(requestId)?.status ==
                                      DownloadStatus.Paused
              ) {
                return@withContext Result.retry()
              }
              e.printStackTrace()
              if (runAttemptCount < 3) {
                return@withContext Result.retry()
              }
              DownloadQueueRepository.updateStatus(requestId, DownloadStatus.Failed)
              db.historyDao()
                      .insertHistory(
                              HistoryEntity(
                                      id = requestIdStr,
                                      url = url,
                                      title = title,
                                      formatId = formatId,
                                      thumbnailUrl = thumbnailUrl,
                                      timestampMs = System.currentTimeMillis(),
                                      totalBytes = totalBytes,
                                      status = DownloadStatus.Failed,
                                      fileUri = null
                              )
                      )
              showFailureNotification(title)
              Result.failure()
            }
          }

  private fun showSuccessNotification(title: String, uriString: String, mimeType: String) {
    val intent =
            Intent(Intent.ACTION_VIEW).apply {
              setDataAndType(Uri.parse(uriString), mimeType)
              addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

    val pendingIntent =
            PendingIntent.getActivity(
                    applicationContext,
                    id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

    val notification =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setContentTitle("Download Complete")
                    .setContentText(title)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

    notificationManager.notify(id.hashCode() + 1, notification)
  }

  private fun showFailureNotification(title: String) {
    val notification =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setContentTitle("Download Failed")
                    .setContentText(title)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setAutoCancel(true)
                    .build()

    notificationManager.notify(id.hashCode() + 1, notification)
  }

  companion object {
    const val KEY_URL = "key_url"
    const val KEY_FORMAT_ID = "key_format_id"
    const val KEY_REQUEST_ID = "key_request_id"
    const val KEY_TITLE = "key_title"
    const val KEY_THUMBNAIL = "key_thumbnail"
    const val KEY_TOTAL_BYTES = "key_total_bytes"
    const val CHANNEL_ID = "vidown_downloads"

    const val ACTION_PAUSE = "app.vidown.action.PAUSE"
    const val ACTION_RESUME = "app.vidown.action.RESUME"
  }
}
