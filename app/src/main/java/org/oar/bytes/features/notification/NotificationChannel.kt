package org.oar.bytes.features.notification

import android.content.Context
import androidx.annotation.StringRes
import org.oar.bytes.R

enum class NotificationChannel(
    val id: String,
    @StringRes val stringId: Int
) {
    LEVEL_AVAILABLE(
        "level-available",
        R.string.level_available_notification_text
    ),
    MAX_CAPACITY_REACHED(
        "max-capacity-reached",
        R.string.max_capacity_reached_notification_text
    ),
    IDLE_ENDED(
        "idle-ended",
        R.string.idle_ended_notification_text
    );

    val silentId: String get() = "$id-silent"
    fun toString(context: Context) = context.getString(stringId)
    fun toSilentString(context: Context) = "${toString(context)} (silent notification)"

    companion object {
        fun fromId(id: String): NotificationChannel =
            values().find { it.id == id }
                ?: throw IllegalArgumentException("NotificationChannel with id \"$id\" not found")
    }
}