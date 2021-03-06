package fr.hazegard.drfreeze

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


/**
 * This class manage get the preferences set by the user
 */
@Singleton
class PreferencesHelper @Inject constructor(
        @Named("Shared_preferences") val defaultSharedPreferences: SharedPreferences,
        private val context: Context) {
    /**
     * Get the preference on system apps listing
     * @return whether the User choose to manage system apps
     */
    fun isSystemAppsEnabled(): Boolean {
        return defaultSharedPreferences.getBoolean(
                context.getString(R.string.preferences_show_system_apps),
                false
        )
    }

    /**
     * Get the preference on system apps listing
     * @return whether the User choose to manage system apps
     */
    fun isOnlyLauncherApp(): Boolean {
        return defaultSharedPreferences.getBoolean(
                context.getString(R.string.preferences_show_only_launcher_apps),
                true
        )
    }

    /**
     * Get the preference on boot notification
     * @return whether the user choose to enable notification on boot
     */
    fun isBootNotificationDisabled(): Boolean {
        return defaultSharedPreferences.getBoolean(
                context.getString(R.string.preferences_disable_boot_notification),
                false
        )
    }

    /**
     * Get the preference on notifications
     * @return whether the user choose to disable notifications
     */
    fun isNotificationDisabled(): Boolean {
        return defaultSharedPreferences.getBoolean(
                context.getString(R.string.preferences_disable_notification),
                false
        )
    }

    /**
     * Get the preference on persistent notifications
     * @return whether the user choose to disable the persistence of notifications
     */
    fun isNotificationPersistent(): Boolean {
        return defaultSharedPreferences.getBoolean(
                context.getString(R.string.preferences_disable_persistent_notification),
                true
        )
    }

    /**
     * Update the shared preferences to bypass the not rooted screen
     *
     */
    fun setBypassRootNeeded() {
        defaultSharedPreferences
                .edit()
                .putBoolean(context.getString(R.string.preferences_bypass_root_needed), true)
                .apply()
    }

    /**
     * Get the preference to whether show not rooted screen
     *
     */
    fun doBypassRootNeeded(): Boolean {
        return defaultSharedPreferences.getBoolean(
                context.getString(R.string.preferences_bypass_root_needed),
                false
        )
    }
}