package app.vidown.domain.manager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

sealed class UpdateResult {
    data class UpdateAvailable(val version: String, val downloadUrl: String) : UpdateResult()
    data object UpToDate : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

class UpdateManager(private val context: Context) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val githubApiUrl = "https://api.github.com/repos/islhkafaa/vidown/releases/latest"

    suspend fun checkForUpdates(currentVersionName: String): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(githubApiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext UpdateResult.Error("Failed to check for updates: HTTP ${response.code}")
            }

            val responseBody = response.body?.string() ?: return@withContext UpdateResult.Error("Empty response")
            val releaseObj = json.parseToJsonElement(responseBody).jsonObject

            val tagName = releaseObj["tag_name"]?.jsonPrimitive?.content ?: ""
            val onlineVersion = tagName.removePrefix("v").removePrefix("V")
            val appVersion = currentVersionName.removePrefix("v").removePrefix("V")

            if (isNewerVersion(onlineVersion, appVersion)) {
                val assets = releaseObj["assets"]?.jsonArray
                val apkAsset = assets?.firstOrNull {
                    it.jsonObject["name"]?.jsonPrimitive?.content?.endsWith(".apk") == true
                }?.jsonObject

                val downloadUrl = apkAsset?.get("browser_download_url")?.jsonPrimitive?.content

                if (downloadUrl != null) {
                    return@withContext UpdateResult.UpdateAvailable(onlineVersion, downloadUrl)
                } else {
                    return@withContext UpdateResult.Error("No APK found in the latest release.")
                }
            } else {
                return@withContext UpdateResult.UpToDate
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Check for updates failed", e)
            return@withContext UpdateResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun downloadAndInstallUpdate(downloadUrl: String, filename: String): Boolean = withContext(Dispatchers.IO) {
        try {
            _downloadProgress.value = 0f
            val request = Request.Builder().url(downloadUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                _downloadProgress.value = null
                return@withContext false
            }

            val body = response.body ?: run {
                _downloadProgress.value = null
                return@withContext false
            }

            val totalBytes = body.contentLength()
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) updatesDir.mkdirs()

            val apkFile = File(updatesDir, filename)
            if (apkFile.exists()) apkFile.delete()

            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(apkFile)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                if (totalBytes > 0) {
                    _downloadProgress.value = totalRead.toFloat() / totalBytes
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            _downloadProgress.value = 1f

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            installApk(uri)
            _downloadProgress.value = null
            true
        } catch (e: Exception) {
            Log.e("UpdateManager", "Manual download failed", e)
            _downloadProgress.value = null
            false
        }
    }

    private fun installApk(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to launch install intent", e)
        }
    }

    private fun isNewerVersion(onlineVersion: String, currentVersion: String): Boolean {
        val onlineParts = onlineVersion.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(onlineParts.size, currentParts.size)
        for (i in 0 until length) {
            val onlinePart = onlineParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }

            if (onlinePart > currentPart) return true
            if (onlinePart < currentPart) return false
        }
        return false
    }
}
