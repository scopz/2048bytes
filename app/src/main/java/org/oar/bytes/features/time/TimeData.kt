package org.oar.bytes.features.time

import org.json.JSONObject

class TimeData(
    val type: Int,
    val id: Int,
    val countOfflineTime: Boolean,
) {

    private var pausedTime = 0L
    var startTime = System.currentTimeMillis()
        private set

    val passedTime
        get() = System.currentTimeMillis() - startTime

    fun addOffset(offset: Long) {
        startTime += offset
    }

    fun pauseTime(pause: Boolean) {
        if (countOfflineTime) return
        if (pause) {
            pausedTime = System.currentTimeMillis()
        } else if (pausedTime > 0) {
            addOffset(System.currentTimeMillis() - pausedTime)
            pausedTime = 0
        }
    }

    fun packJSON(): JSONObject {
        return JSONObject().apply {
            put("t", type)
            put("i", id)
            put("st", startTime)
            put("off", countOfflineTime)
        }
    }

    companion object {
        fun loadJSON(jObj: JSONObject): TimeData {
            val type = jObj.getInt("t")
            val id = jObj.getInt("i")
            val countOffLineTime = jObj.getBoolean("off")

            return TimeData(type, id, countOffLineTime).apply {
                startTime = jObj.getLong("st")
            }
        }
    }
}