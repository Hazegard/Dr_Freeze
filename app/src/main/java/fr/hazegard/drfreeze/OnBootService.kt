package fr.hazegard.drfreeze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by hazegard on 16/03/18.
 */
/**
 * Boot receiver
 */
class OnBootService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action
                && !PreferencesHelper(context).isBootNotificationDisabled()) {
            val enabledAndTrackedApps = PackageManager(context).getEnabledAndTracked()
            NotificationUtils.sendNotification(context, enabledAndTrackedApps)
        }
    }

    companion object {
        private const val TAG = "OnBootService"
    }
}