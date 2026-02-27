package app.vidown.data.repository

import android.content.Context
import android.util.Log
import app.vidown.domain.models.VideoInfo
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class YtDlpRepository(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    companion object {
        private val initMutex = Mutex()
        private var isInitialized = false
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            initMutex.withLock {
                if (!isInitialized) {
                    try {
                        withContext(Dispatchers.IO) {
                            YoutubeDL.getInstance().init(context)
                            FFmpeg.getInstance().init(context)
                            isInitialized = true
                        }
                    } catch (e: Exception) {
                        Log.e("YtDlpRepository", "Initialization failed", e)
                        throw e
                    }
                }
            }
        }
    }

    suspend fun fetchVideoInfo(url: String, allowPlaylist: Boolean = false): Result<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            ensureInitialized()

            val request = YoutubeDLRequest(url).apply {
                addOption("--dump-json")
                if (!allowPlaylist) {
                    addOption("--no-playlist")
                } else {
                    addOption("--flat-playlist")
                }
            }

            val response = YoutubeDL.getInstance().execute(request)
            val output = response.out

            val jsonString = output.lines().lastOrNull { it.trim().startsWith("{") }

            if (jsonString.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Empty response from yt-dlp"))
            }

            val videoInfo = json.decodeFromString<VideoInfo>(jsonString)
            Result.success(videoInfo)

        } catch (e: Exception) {
            Log.e("YtDlpRepository", "Error fetching video info", e)
            Result.failure(e)
        }
    }

    suspend fun updateYtDlp(): Result<YoutubeDL.UpdateStatus> = withContext(Dispatchers.IO) {
        try {
            ensureInitialized()
            val status = YoutubeDL.getInstance().updateYoutubeDL(context, YoutubeDL.UpdateChannel._STABLE)
            Result.success(status ?: YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE)
        } catch (e: Exception) {
            Log.e("YtDlpRepository", "Error updating yt-dlp", e)
            Result.failure(e)
        }
    }
}
