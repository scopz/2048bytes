package org.oar.bytes.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import org.oar.bytes.R

class MainSettingsView(
    context: Context,
    attrs: AttributeSet? = null
) : MainView(context, attrs, R.layout.fragment_general_settings) {

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            listOf("stop", "start")
                .mapNotNull { findPreference<EditTextPreference>(it) }
                .forEach {
                    it.setOnBindEditTextListener { editText -> editText.setSingleLine() }
                    it.setOnPreferenceChangeListener { _, newValue ->
                        val matches = TIME_REGEX.matches(newValue as String)
                        if (!matches) {
                            Toast.makeText(
                                context,
                                requireContext().getString(R.string.time_error_format),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        matches
                    }
                }
        }

        companion object {
            private val TIME_REGEX = Regex("^(?:1?\\d|2[0-3]):[0-5]\\d\$")
        }
    }
}