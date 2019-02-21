package fr.hazegard.drfreeze

import android.app.IntentService
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import javax.inject.Inject

class StopBatchUpdateService : IntentService("BatchUpdateService") {

    @Inject
    lateinit var batchUpdate: BatchUpdate

    override fun onHandleIntent(intent: Intent?) {
        AndroidInjection.inject(this)
        if (intent?.action?.equals(ACTION_STOP) == true) {
            batchUpdate.disableUpdateMode()
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