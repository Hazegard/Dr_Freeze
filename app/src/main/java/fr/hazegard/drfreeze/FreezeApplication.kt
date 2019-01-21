package fr.hazegard.drfreeze

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import android.widget.Toast
import fr.hazegard.drfreeze.ui.NotRootActivity

class FreezeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Su.isRootAvailable) {
            if (PreferencesHelper.doBypassRootNeeded(this)) {
                Toast.makeText(this, getString(R.string.no_root_warning), Toast.LENGTH_LONG).show()
            } else {
                startActivity(NotRootActivity.newIntent(this))
            }
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
}