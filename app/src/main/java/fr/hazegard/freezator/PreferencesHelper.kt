package fr.hazegard.freezator

import android.content.Context
import android.preference.PreferenceManager


/**
 * This class manage get the preferences set by the user
 */
class PreferencesHelper {
    companion object {
        /**
         * Get the preference on system apps listing
         * @return whether the User choose to manage system apps
         */
        fun isSystemAppsEnabled(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.preferences_show_system_apps), false)
        }

        /**
         * Get the preference on boot notification
         * @return whether the user choose to enable notification on boot
         */
        fun isBootNotificationDisabled(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.preferences_disable_boot_notification), false)
        }

        /**
         * Get the preference on notifications
         * @return whether the user choose to disable notifications
         */
        fun isNotificationDisabled(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.preferences_disable_notification), false)
        }

        /**
         * Get the preference on persistent notifications
         * @return whether the user choose to disable the persistence of notifications
         */
        fun isNotificationPersistent(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.preferences_disable_persistent_notification), true)
        }
    }
}