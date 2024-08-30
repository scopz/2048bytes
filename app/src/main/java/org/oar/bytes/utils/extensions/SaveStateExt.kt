package org.oar.bytes.utils.extensions

import android.content.Context
import android.content.ContextWrapper
import org.json.JSONException
import org.json.JSONObject
import org.oar.bytes.utils.Constants
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.PrintWriter

object SaveStateExt {

    private val Context.statePath: String
        get() = ContextWrapper(this).filesDir.path + "/" + Constants.STATE_FILENAME

    fun Context.saveState(obj: JSONObject) {

        try {
            PrintWriter(statePath).use {
                it.print(obj)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun Context.deleteState() {
        File(statePath).delete()
    }

    fun Context.hasState(): Boolean {
        return File(statePath).exists()
    }

    fun Context.loadState(): JSONObject? {
        val statePath = statePath

        if (File(statePath).exists()) {
            try {
                val line = BufferedReader(FileReader(statePath)).use {
                    it.readLine()
                }

                return JSONObject(line)
            } catch (e: JSONException) {
                System.err.println("Couldn't load \"$statePath\"")
            } catch (e: IOException) {
                System.err.println("Couldn't load \"$statePath\"")
            }
        }
        return null
    }
}