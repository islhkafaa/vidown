package app.vidown.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.vidown.domain.models.DownloadStatus

@Entity(tableName = "download_history")
data class HistoryEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val title: String,
    val formatId: String,
    val thumbnailUrl: String?,
    val timestampMs: Long,
    val durationInSeconds: Int? = null,
    val totalBytes: Long,
    val status: DownloadStatus,
    val fileUri: String? = null
)
