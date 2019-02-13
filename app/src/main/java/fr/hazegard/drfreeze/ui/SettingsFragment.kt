package fr.hazegard.drfreeze.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import fr.hazegard.drfreeze.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}