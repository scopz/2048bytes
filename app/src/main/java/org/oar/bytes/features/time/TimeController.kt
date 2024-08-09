package org.oar.bytes.features.time

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.oar.bytes.utils.JsonExt.getJSONArrayOrNull
import org.oar.bytes.utils.JsonExt.getLongOrNull
import org.oar.bytes.utils.JsonExt.jsonArray
import org.oar.bytes.utils.JsonExt.map
import org.oar.bytes.utils.Utils.hashOf
import java.util.concurrent.ConcurrentHashMap

object TimeController {
    private val timedReferences = mutableMapOf<Int, TimeControlled>()
    private val timeData = ConcurrentHashMap<Int, TimeData>()
    private var lastShutdownTime = 0L

    fun register(type: Int, id: Int, timeControlled: TimeControlled) {
        val hashId = hashOf(type, id)
        timedReferences[hashId] = timeControlled
    }

    fun startTimeData(type: Int, id: Int, countOfflineTime: Boolean) {
        val hashId = hashOf(type, id)
        if (timedReferences.containsKey(hashId)) {
            timeData[hashId] = TimeData(type, id, countOfflineTime)
        } else {
            throw TimeException("TimeData entered for unknown reference")
        }
    }

    fun removeTimeData(type: Int, id: Int) {
        val hashId = hashOf(type, id)
        if (!timedReferences.containsKey(hashId)) {
            throw TimeException("TimeData entered for unknown reference")
        }
        timeData.remove(hashId)
    }

    fun setLastShutdownTime() {
        lastShutdownTime = System.currentTimeMillis()
    }

    fun notifyOfflineTime(context: Context) {
        val currentTime = System.currentTimeMillis()
        val offlineTimePassed = currentTime - lastShutdownTime

        val timeHashes = timeData.keys
        timeHashes.forEach { hashId ->
            val timeData = timeData[hashId] ?: return@forEach
            val timeReference = timedReferences[hashId] ?: return@forEach

            val keepTimeData = if (timeData.countOfflineTime) {
                timeReference.notifyOfflineTime(
                    timeData.startTime,
                    lastShutdownTime - timeData.startTime,
                    offlineTimePassed
                )
            } else {
                timeData.addOffset(offlineTimePassed)
                timeReference.notifyOfflineTime(
                    timeData.startTime,
                    currentTime - timeData.startTime,
                    0)
            }

            if (!keepTimeData) { // finished timeData
                this.timeData.remove(hashId)
            }
        }
    }

    fun appendToJson(json: JSONObject) {
        json.apply {
            put("offTime", lastShutdownTime)

            timeData.values
                .map { it.packJSON() }
                .jsonArray()
                .also { put("timeData", it) }
        }
    }

    fun fromJson(jObj: JSONObject) {
        lastShutdownTime = jObj.getLongOrNull("offTime") ?: 0L

        jObj.getJSONArrayOrNull("timeData")
            ?.map(JSONArray::getJSONObject)
            ?.forEach {
                val timeData = TimeData.loadJSON(it)

                val hashId = hashOf(timeData.type, timeData.id)
                if (!timedReferences.containsKey(hashId)) {
                    throw TimeException("TimeData entered for unknown reference")
                }
                this.timeData[hashId] = timeData
            }
    }
}
