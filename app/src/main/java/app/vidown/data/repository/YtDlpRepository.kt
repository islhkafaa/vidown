package app.vidown.data.repository

import app.vidown.domain.models.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class YtDlpRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    suspend fun fetchVideoInfo(url: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(url).apply {
                addOption("-J")
            }

            val response = YoutubeDL.getInstance().execute(request)
            val output = response.out

            val jsonString = output?.lines()?.lastOrNull { it.trim().startsWith("{") }

            if (jsonString.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Empty response from yt-dlp"))
            }

            val videoInfo = json.decodeFromString<VideoInfo>(jsonString)
            Result.success(videoInfo)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    suspend fun updateYtDlp(): Result<YoutubeDL.UpdateStatus> = withContext(Dispatchers.IO) {
        try {
            val status = YoutubeDL.getInstance().updateYoutubeDL(app.vidown.Application().applicationContext, YoutubeDL.UpdateChannel._STABLE)
            Result.success(status ?: YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
