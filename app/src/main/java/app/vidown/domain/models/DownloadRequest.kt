package app.vidown.domain.models

import java.util.UUID

enum class DownloadStatus {
    Pending, Downloading, Success, Failed
}

data class DownloadRequest(
    val id: UUID = UUID.randomUUID(),
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val formatId: String,
    val status: DownloadStatus = DownloadStatus.Pending,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L
)
