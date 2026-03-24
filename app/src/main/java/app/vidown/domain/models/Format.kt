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
        get() = vcodec != null && vcodec != "none"

    val isAudioOnly: Boolean
        get() = !isVideo && acodec != null && acodec != "none"

    val displaySize: Long
        get() = filesize ?: filesizeApprox ?: 0L

    val hasAudio: Boolean
        get() = acodec != null && acodec != "none"

    val friendlyLabel: String
        get() {
            var label = formatNote ?: resolution ?: if (isAudioOnly) "Audio" else "Standard"

            label = label.replace("DASH video", "", ignoreCase = true)
                .replace("DASH audio", "", ignoreCase = true)
                .replace("DASH", "", ignoreCase = true)
                .trim()

            if (label.isEmpty()) {
                label = resolution ?: if (height != null) "${height}p" else "Standard"
            }

            return when {
                isVideo && hasAudio -> "$label (Direct)"
                isVideo -> "$label (High Quality)"
                isAudioOnly -> "$label (Audio)"
                else -> label
            }
        }
}
