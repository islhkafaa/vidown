package app.vidown.data.local

import androidx.room.TypeConverter
import app.vidown.domain.models.DownloadStatus

class Converters {
    @TypeConverter
    fun fromDownloadStatus(status: DownloadStatus): String {
        return status.name
    }

    @TypeConverter
    fun toDownloadStatus(name: String): DownloadStatus {
        return try {
            DownloadStatus.valueOf(name)
        } catch (e: IllegalArgumentException) {
            DownloadStatus.Failed
        }
    }
}
