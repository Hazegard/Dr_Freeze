package fr.hazegard.drfreeze

import android.app.IntentService
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.android.AndroidInjection
import fr.hazegard.drfreeze.ui.ManageTrackedAppActivity
import javax.inject.Inject

class StopBatchUpdateService : IntentService("BatchUpdateService") {

    @Inject
    lateinit var batchUpdate: BatchUpdate

    /**
     * Intent received when the user click on the notification to disable the batch update mode
     */
    override fun onHandleIntent(intent: Intent?) {
        AndroidInjection.inject(this)
        if (intent?.action?.equals(ACTION_STOP) == true) {
            batchUpdate.disableUpdateMode()
            val i = Intent().apply {
                action = ManageTrackedAppActivity.ACTION_STOP_BATCH_UPDATE
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(i)
        }
    }

    companion object {
        const val ACTION_STOP = "ACTION_STOP"
        fun newIntent(context: Context): Intent {
            return Intent(context, StopBatchUpdateService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }
}