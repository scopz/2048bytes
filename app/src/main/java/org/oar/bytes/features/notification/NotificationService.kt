package org.oar.bytes.features.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service.NOTIFICATION_SERVICE
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
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
        context.apply {
            val notificationManager = getSystemService(NotificationManager::class.java)

            listOf(
                createChannel(LEVEL_AVAILABLE),
                createChannel(MAX_CAPACITY_REACHED),
                createChannel(IDLE_ENDED),

                createSilentChannel(LEVEL_AVAILABLE),
                createSilentChannel(MAX_CAPACITY_REACHED),
                createSilentChannel(IDLE_ENDED),

            ).forEach {
                it.enableLights(true)
                it.lightColor = Color.YELLOW
                notificationManager.createNotificationChannel(it)
            }

            prioritizeNotifications()
        }
    }

    private fun Context.createChannel(channel: NotificationChannel) =
        android.app.NotificationChannel(
            channel.id,
            channel.toString(this),
            NotificationManager.IMPORTANCE_HIGH
        )

    private fun Context.createSilentChannel(channel: NotificationChannel) =
        android.app.NotificationChannel(
            channel.silentId,
            channel.toSilentString(this),
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setSound(null, null)
            }

    @SuppressLint("BatteryLife")
    private fun Context.prioritizeNotifications() {
        val packageName = this.packageName
        val powerManager = this.getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent().apply {
                setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                setData(Uri.parse("package:$packageName"))
            }
            this.startActivity(intent)
        }
    }
}