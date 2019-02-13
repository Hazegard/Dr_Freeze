package fr.hazegard.drfreeze.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import fr.hazegard.drfreeze.R


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (isAdded) {
            when (key) {
                getString(R.string.preferences_show_system_apps) -> {
                    callback?.onFilterSystemAppChanged()
                }
                getString(R.string.preferences_show_only_launcher_apps) -> {
                    callback?.onFilterLauncherAppChanged()
                }
                getString(R.string.preferences_disable_notification) -> {
                    callback?.onNotificationChanged()
                }
            }
//            if (key == getString(R.string.preferences_show_system_apps) || key == getString(R.string.preferences_show_only_launcher_apps)) {
//                callback?.onFilterSystemAppChanged()
//            }
//            if (key == getString(R.string.preferences_disable_notification)) {
//
//            }
        }
    }

    private var callback: OnSettingChangeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = activity as OnSettingChangeListener
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    interface OnSettingChangeListener {
        fun onFilterSystemAppChanged()
        fun onFilterLauncherAppChanged()
        fun onNotificationChanged()
    }
}