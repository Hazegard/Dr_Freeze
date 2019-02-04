package fr.hazegard.drfreeze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import javax.inject.Inject

/**
 * Created by hazegard on 16/03/18.
 */
/**
 * Boot receiver
 */
class OnBootService : BroadcastReceiver() {
    @Inject
    lateinit var packageManager: PackageManager

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent?) {
        AndroidInjection.inject(this, context)
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action
                && !preferencesHelper.isBootNotificationDisabled()) {
            val enabledAndTrackedApps = packageManager.getEnabledAndTracked()
            if (enabledAndTrackedApps.isNotEmpty()) {
                notificationManager.sendNotification(enabledAndTrackedApps)
            }
        }
    }

    companion object {
        private const val TAG = "OnBootService"
    }
}