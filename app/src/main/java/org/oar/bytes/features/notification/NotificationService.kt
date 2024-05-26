package org.oar.bytes.features.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service.NOTIFICATION_SERVICE
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.graphics.Color
import org.oar.bytes.features.notification.NotificationChannel.IDLE_ENDED
import org.oar.bytes.features.notification.NotificationChannel.LEVEL_AVAILABLE
import org.oar.bytes.features.notification.NotificationChannel.MAX_CAPACITY_REACHED


object NotificationService {

    private val pendingIntents = mutableListOf<PendingIntent>()

    fun Context.scheduleNotification(channel: NotificationChannel, delayInSeconds: Int) {
        val triggerTime = System.currentTimeMillis() + delayInSeconds * 1000

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            action = channel.id
            putExtra("moment", triggerTime)
            flags = flags or Intent.FLAG_RECEIVER_FOREGROUND
        }

        val scheduledIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        ).apply(pendingIntents::add)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, scheduledIntent)
        } catch(_: SecurityException) {

        }
    }

    fun Context.clearScheduledNotifications() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        NotificationChannel.values().forEach {
            val intent = Intent(this, NotificationReceiver::class.java).apply {
                action = it.id
                flags = flags or Intent.FLAG_RECEIVER_FOREGROUND
            }

            val scheduledIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.cancel(scheduledIntent)
            notificationManager.cancel(it.id.length)
        }
    }

    fun configureChannels(context: Context) {
        val notificationManager = context
            .getSystemService(NotificationManager::class.java)

        createChannel(context, LEVEL_AVAILABLE).apply(notificationManager::createNotificationChannel)
        createChannel(context, MAX_CAPACITY_REACHED).apply(notificationManager::createNotificationChannel)
        createChannel(context, IDLE_ENDED).apply(notificationManager::createNotificationChannel)

        createSilentChannel(context, LEVEL_AVAILABLE).apply(notificationManager::createNotificationChannel)
        createSilentChannel(context, MAX_CAPACITY_REACHED).apply(notificationManager::createNotificationChannel)
        createSilentChannel(context, IDLE_ENDED).apply(notificationManager::createNotificationChannel)
    }

    private fun createChannel(context: Context, channel: NotificationChannel) =
        android.app.NotificationChannel(
            channel.id,
            channel.toString(context),
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                enableLights(true)
                lightColor = Color.YELLOW
            }

    private fun createSilentChannel(context: Context, channel: NotificationChannel) =
        android.app.NotificationChannel(
            channel.silentId,
            channel.toSilentString(context),
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setSound(null, null)
                enableLights(true)
                lightColor = Color.YELLOW
            }
}