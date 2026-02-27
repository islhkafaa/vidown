package app.vidown.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.vidown.MainActivity
import java.util.UUID

object VidownNotificationManager {
    private const val CHANNEL_ID = "vidown_downloads"
    private const val UPDATES_CHANNEL_ID = "vidown_updates"

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

    fun showUpdateNotification(context: Context, newVersion: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, UPDATES_CHANNEL_ID)
            .setContentTitle("Update Available")
            .setContentText("Version $newVersion is ready to download.")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notification)
    }

    fun showPlaylistCompleteNotification(context: Context, playlistTitle: String, count: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Playlist Download Complete")
            .setContentText("Downloaded $count videos from $playlistTitle")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(UUID.randomUUID().hashCode(), notification)
    }
}
