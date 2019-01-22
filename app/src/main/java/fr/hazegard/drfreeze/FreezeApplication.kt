package fr.hazegard.drfreeze

import android.app.Application
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import fr.hazegard.drfreeze.injection.AppComponent
import fr.hazegard.drfreeze.injection.AppModule
import fr.hazegard.drfreeze.injection.DaggerAppComponent
import fr.hazegard.drfreeze.injection.SuModule
import fr.hazegard.drfreeze.ui.NotRootActivity

class FreezeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (!Su.isRootAvailable) {
            if (PreferencesHelper(this).doBypassRootNeeded()) {
                Toast.makeText(this, getString(R.string.no_root_warning), Toast.LENGTH_LONG).show()
            } else {
                startActivity(NotRootActivity.newIntent(this))
            }
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initDagger()
    }

    private fun initDagger() {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .suModule(SuModule())
                .build()
    }

    companion object {
        lateinit var appComponent: AppComponent
    }
}