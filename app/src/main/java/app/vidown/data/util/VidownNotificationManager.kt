package app.vidown.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object VidownNotificationManager {
    private const val CHANNEL_ID = "vidown_downloads"
    private const val UPDATES_CHANNEL_ID = "vidown_updates"

    fun init(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val downloadChannel = NotificationChannel(
            CHANNEL_ID,
            "Downloads",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Download progress and status"
        }

        val updatesChannel = NotificationChannel(
            UPDATES_CHANNEL_ID,
            "App Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for new app versions"
        }

        notificationManager.createNotificationChannel(downloadChannel)
        notificationManager.createNotificationChannel(updatesChannel)
    }
}
