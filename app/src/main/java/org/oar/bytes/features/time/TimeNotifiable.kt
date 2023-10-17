package org.oar.bytes.features.time

import android.app.Notification

interface TimeNotifiable {
    fun getNotificationChannel(): Notification.Builder
    fun getRelativeFinishDate(): Long
}