package org.oar.bytes.features.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.oar.bytes.R
import org.oar.bytes.ui.InitActivity
import java.time.LocalDateTime

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = intent.action
            ?.let(NotificationChannel::fromId)
            ?: return

        val notificationIntent = Intent(context, InitActivity::class.java)

        val pendingIntent = TaskStackBuilder.create(context)
            .run {
                addNextIntentWithParentStack(notificationIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        val channelId = if (mustBeSilent) channel.silentId else channel.id

        val text = channel.toString(context)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(channel.id.length, builder.build())
    }

    private val mustBeSilent: Boolean
        get() {
            val time = LocalDateTime.now()
            return when {
                time.hour == 20 && time.minute >= 30 -> true
                time.hour > 20 -> true
                time.hour < 6 -> true
                time.hour == 6 && time.minute <= 30 -> true
                else -> false
            }
        }
}