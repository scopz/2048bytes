package org.oar.bytes.utils.extensions

import org.json.JSONArray
import org.json.JSONObject

object JsonExt {
    fun <T> Collection<T>.jsonArray() = JSONArray().also { this.forEach { obj -> it.put(obj) } }

    fun <T> JSONArray.map(function: (JSONArray, Int) -> T) =
        (0 until this.length()).map { function(this, it) }

    fun <T> JSONArray.mapJsonObject(function: (JSONObject) -> T) =
        map(JSONArray::getJSONObject).map(function)

    fun <T> JSONArray.mapJsonArray(function: (JSONArray) -> T) =
        map(JSONArray::getJSONArray).map(function)

    fun <T> JSONArray.mapInt(function: (Int) -> T) =
        map(JSONArray::getInt).map(function)

    fun JSONObject.getJSONArrayOrNull(name: String) =
        if (has(name)) getJSONArray(name) else null

    fun JSONObject.getStringOrNull(name: String) =
        if (has(name)) getString(name) else null

    fun JSONObject.getIntOrNull(name: String) =
        if (has(name)) getInt(name) else null

    fun JSONObject.getLongOrNull(name: String) =
        if (has(name)) getLong(name) else null
}