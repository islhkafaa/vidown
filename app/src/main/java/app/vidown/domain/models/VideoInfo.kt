package app.vidown.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(
    val id: String = "",
    @SerialName("webpage_url")
    val url: String = "",
    val title: String = "",
    val description: String? = null,
    val duration: Double? = null,
    val uploader: String? = null,
    val ext: String? = null,
    @SerialName("thumbnail")
    val thumbnailUrl: String? = null,
    val formats: List<Format> = emptyList(),
    val entries: List<VideoInfo>? = null
)
