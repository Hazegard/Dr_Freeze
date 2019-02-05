package fr.hazegard.drfreeze

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.android.AndroidInjection

class OnApplicationRemovedService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG,"##########")
        AndroidInjection.inject(this, context)
        if (Intent.ACTION_MEDIA_REMOVED == intent?.action
                && intent.extras?.getBoolean(Intent.EXTRA_REPLACING) == true) {
            val deletedPkg = intent.data?.schemeSpecificPart
            Log.d(TAG, deletedPkg)
        }
    }

    companion object {
        private const val TAG = "OnApplicationRemoved"
    }
}