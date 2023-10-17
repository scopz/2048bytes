package org.oar.bytes.features.time

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import org.oar.bytes.R
import org.oar.bytes.ui.GridActivity

object NotificationPublisher : BroadcastReceiver() {

    private const val DELAYED_KEY_NOTIFICATION_ID = "not_id"
    private const val DELAYED_KEY_NOTIFICATION = "noti"
    private const val BASIC_NOTIFICATION_ID = "notification_id"
    private const val BASIC_NOTIFICATION_NAME = "notification"

    fun notify(context: Context, id: Int, title: String, body: String) {
        val mManager = getManager(context)
        createNotificationChannel(mManager)

        val notificationBuilder = getChannelNotification(context, title, body)
        mManager.notify(id, notificationBuilder.build())
    }

    fun cancel(context: Context, id: Int) {
        getManager(context).cancel(id)
    }

    fun notify(context: Context, id: Int, title: String, body: String, delay: Long) {
        val notificationBuilder = getChannelNotification(context, title, body)
        notify(context, id, notificationBuilder, delay)
    }

    fun notify(context: Context, id: Int, nb: Notification.Builder, delay: Long) {
        val mManager = getManager(context)
        createNotificationChannel(mManager)
        scheduleNotification(context, nb, delay, id)
    }

    fun cancelDelayed(context: Context, id: Int) {
        val intent = Intent(context, NotificationPublisher::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        cancel(context, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notification = intent.getParcelableExtra<Notification>(DELAYED_KEY_NOTIFICATION)
        val notificationId = intent.getIntExtra(DELAYED_KEY_NOTIFICATION_ID, 0)
        getManager(context).notify(notificationId, notification)
    }

    private fun getManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotificationChannel(mManager: NotificationManager) {
        val nChannel = NotificationChannel(
            BASIC_NOTIFICATION_ID,
            BASIC_NOTIFICATION_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        // Sets whether notifications posted to this channel should display notification lights
        nChannel.enableLights(true)
        // Sets whether notification posted to this channel should vibrate.
        nChannel.enableVibration(true)
        // Sets the notification light color for notifications posted to this channel
        nChannel.lightColor = Color.YELLOW
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        nChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        mManager.createNotificationChannel(nChannel)
    }

    fun getChannelNotification(
        context: Context,
        title: String,
        body: String
    ): Notification.Builder {
        return Notification.Builder(context, BASIC_NOTIFICATION_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.battery_plus_24px)
            .setAutoCancel(true)
    }

    private fun scheduleNotification(
        context: Context,
        builder: Notification.Builder,
        delay: Long,
        notificationId: Int
    ) { //delay is after how much time(in millis) from current time you want to schedule the notification
        val intent = Intent(context, GridActivity::class.java)
        intent.putExtra("autoContinue", true)
        val activity = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        builder.setContentIntent(activity)
        val notification = builder.build()
        val notificationIntent = Intent(context, NotificationPublisher::class.java)
        notificationIntent.putExtra(DELAYED_KEY_NOTIFICATION_ID, notificationId)
        notificationIntent.putExtra(DELAYED_KEY_NOTIFICATION, notification)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val futureInMillis = SystemClock.elapsedRealtime() + delay
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis] = pendingIntent
    }
}