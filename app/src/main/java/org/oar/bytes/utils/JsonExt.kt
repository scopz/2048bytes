package org.oar.bytes.utils

import org.json.JSONArray
import org.json.JSONObject

object JsonExt {
    fun <T> List<T>.jsonArray() = JSONArray().also { this.forEach { obj -> it.put(obj) } }

    fun <T> JSONArray.mapJsonObject(function: (JSONObject) -> T) =
        map(function) { getJSONObject(it) }

    fun <T> JSONArray.mapJsonArray(function: (JSONArray) -> T) =
        map(function) { getJSONArray(it) }

    fun <T> JSONArray.mapInt(function: (Int) -> T) =
        map(function) { getInt(it) }

    private fun <O,T> JSONArray.map(function: (O) -> T, getFunction: (Int) -> O): List<T> {
        return mutableListOf<T>().also { list ->
            val length = this.length()
            for (i in 0 until length) {
                list.add(function(getFunction(i)))
            }
        }
    }
}