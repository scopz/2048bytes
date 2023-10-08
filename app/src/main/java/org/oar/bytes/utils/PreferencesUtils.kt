package org.oar.bytes.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PreferencesUtils {

    private val Context.preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(this)

    private val Context.preferencesEditor: SharedPreferences.Editor
        get() = preferences.edit()

    fun Context.save(name: String, content: String) {
        preferencesEditor.apply {
            putString(name, content)
            apply()
        }
    }

    fun Context.save(name: String, content: Int) {
        preferencesEditor.apply {
            putInt(name, content)
            apply()
        }
    }

    fun Context.save(name: String, content: Boolean) {
        preferencesEditor.apply {
            putBoolean(name, content)
            apply()
        }
    }

    fun Context.remove(name: String) {
        preferencesEditor.apply {
            remove(name)
            apply()
        }
    }

    fun Context.loadString(name: String, def: String = ""): String {
        return preferences.getString(name, def) ?: def
    }

    fun Context.loadInteger(name: String, def: Int = 0): Int {
        return preferences.getInt(name, def)
    }

    fun Context.loadBoolean(name: String, def: Boolean = false): Boolean {
        return preferences.getBoolean(name, def)
    }

    fun Context.loadNullableString(name: String, def: String? = null): String? {
        return if (preferences.contains(name)) preferences.getString(name, null)
        else def
    }
}