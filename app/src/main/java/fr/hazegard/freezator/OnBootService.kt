package fr.hazegard.freezator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fr.hazegard.freezator.exception.NotRootException

/**
 * Created by hazegard on 16/03/18.
 */
/**
 * Boot receiver
 */
class OnBootService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action
                && !PreferencesHelper.isBootNotificationDisabled(context)) {
            try {
                val enabledAndTrackedApps = PackageManager(context).getEnabledAndTracked()
                NotificationUtils.sendNotification(context, enabledAndTrackedApps)
            } catch (e: NotRootException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val TAG = "OnBootService"
    }
}