package org.oar.bytes.ui

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import org.oar.bytes.R
import org.oar.bytes.features.notification.NotificationService
import org.oar.bytes.utils.ComponentsExt.overridePendingSideTransition


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                    overridePendingSideTransition(false)
                }
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

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

            findPreference<ListPreference>("led")?.setOnPreferenceChangeListener { _, _ ->
                NotificationService.configureChannels(requireContext())
                true
            }
        }

        companion object {
            private val TIME_REGEX = Regex("^(?:1?\\d|2[0-3]):[0-5]\\d\$")
        }
    }
}