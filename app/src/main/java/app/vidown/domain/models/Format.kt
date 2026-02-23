package app.vidown.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Format(
    @SerialName("format_id")
    val formatId: String = "",
    @SerialName("format_note")
    val formatNote: String? = null,
    val ext: String = "",
    val resolution: String? = null,
    val filesize: Long? = null,
    @SerialName("filesize_approx")
    val filesizeApprox: Long? = null,
    val vcodec: String? = null,
    val acodec: String? = null,
    val height: Int? = null,
    val width: Int? = null,
    val fps: Double? = null,
) {
    val isVideo: Boolean
        get() = vcodec != "none"

    val isAudioOnly: Boolean
        get() = vcodec == "none"

    val displaySize: Long
        get() = filesize ?: filesizeApprox ?: 0L
}
