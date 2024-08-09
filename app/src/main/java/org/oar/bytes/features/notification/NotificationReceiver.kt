package org.oar.bytes.features.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import org.oar.bytes.R
import org.oar.bytes.ui.InitActivity
import org.oar.bytes.utils.PreferencesExt.loadString
import java.time.LocalDateTime

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val moment = intent.getLongExtra("moment", System.currentTimeMillis())
        val channel = intent.action
            ?.let(NotificationChannel::fromId)
            ?: return

        val notificationIntent = Intent(context, InitActivity::class.java)

        val pendingIntent = TaskStackBuilder.create(context)
            .run {
                addNextIntentWithParentStack(notificationIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        val color = when(context.loadString("led", "yellow")) {
            "magenta" -> Color.MAGENTA
            "cyan" -> Color.CYAN
            "blue" -> Color.BLUE
            "white" -> Color.WHITE
            else -> Color.YELLOW
        }

        val channelId = if (mustBeSilent(context)) channel.silentId else channel.id

        val text = channel.toString(context)

        val builder = NotificationCompat.Builder(context, "$channelId$color")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setUsesChronometer(true)
            .setWhen(moment)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(channel.id.length, builder.build())
    }

    private fun mustBeSilent(context: Context): Boolean {
        val stop = context.loadString("stop", "20:30").timeSplit()
        val start = context.loadString("start", "6:30").timeSplit()

        val time = LocalDateTime.now()
        return if (stop < start) { // stop 10:00 - start 16:00
            when {
                time >= start -> false
                time < stop -> false
                else -> true
            }
        } else { // stop 16:00 - start 10:00
            when {
                time >= stop -> true
                time < start -> true
                else -> false
            }
        }
    }

    private fun String.timeSplit() =
        this.split(":")
            .map(String::toInt)

    operator fun List<Int>.compareTo(other: List<Int>): Int =
        this[0].compareTo(other[0])
            .takeIf { it != 0 }
            ?: this[1].compareTo(other[1])

    operator fun LocalDateTime.compareTo(other: List<Int>): Int =
        hour.compareTo(other[0])
            .takeIf { it != 0 }
            ?: minute.compareTo(other[1])
}