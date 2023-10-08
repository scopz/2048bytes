package org.oar.bytes.utils

import org.json.JSONArray
import org.json.JSONObject

object JsonExt {
    fun <T> List<T>.jsonArray() = JSONArray().also { this.forEach { obj -> it.put(obj) } }

    fun <T> JSONArray.map(function: (JSONArray, Int) -> T): List<T> {
        return mutableListOf<T>().also { list ->
            val length = this.length()
            for (i in 0 until length) {
                list.add(function(this, i))
            }
        }
    }

    fun <T> JSONArray.mapJsonObject(function: (JSONObject) -> T): List<T> {
        return mutableListOf<T>().also { list ->
            val length = this.length()
            for (i in 0 until length) {
                list.add(function(this.getJSONObject(i)))
            }
        }
    }
}