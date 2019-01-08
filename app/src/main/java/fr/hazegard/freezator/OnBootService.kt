package fr.hazegard.freezator

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import fr.hazegard.freezator.exception.NotRootException

/**
 * Created by hazegard on 16/03/18.
 */
class OnBootService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
            Log.d(TAG, "Received")
            Test.startListMonitoredApp(context)
        }
    }

    class Test : IntentService("ListMonitoredApp") {
        override fun onHandleIntent(intent: Intent?) {
            if (intent?.action == ACTION_LIST_MONITORED_APP) {
                try {
                    val monitoredAndEnabledApps = AppsManager(this).getEnabledAndMonitored()
                    monitoredAndEnabledApps.forEach {
                        NotificationUtils.notify(this, it)
                    }
                } catch (e: NotRootException) {
                    e.printStackTrace()
                }
            }
        }

        companion object {
            fun startListMonitoredApp(context: Context) {
                val intent = Intent(context, Test::class.java)
                intent.action = ACTION_LIST_MONITORED_APP
                context.startService(intent)
            }
            const val ACTION_LIST_MONITORED_APP = "ACTION_LIST_MONITORED_APP"
        }
    }

    companion object {
        private const val TAG = "OnBootService"
    }
}