package fr.hazegard.drfreeze

import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasServiceInjector
import fr.hazegard.drfreeze.injection.AppComponent
import fr.hazegard.drfreeze.injection.AppModule
import fr.hazegard.drfreeze.injection.DaggerAppComponent
import fr.hazegard.drfreeze.injection.SuModule
import fr.hazegard.drfreeze.ui.NotRootActivity
import javax.inject.Inject

class FreezeApplication : Application(),
        HasBroadcastReceiverInjector,
        HasServiceInjector {

    @Inject
    lateinit var intentServiceInjector: DispatchingAndroidInjector<Service>

    override fun serviceInjector(): AndroidInjector<Service> {
        return intentServiceInjector
    }

    @Inject
    lateinit var broadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return broadcastReceiverInjector
    }

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initDagger()
        appComponent.inject(this)
        if (!Su.isRootAvailable) {
            if (preferencesHelper.doBypassRootNeeded()) {
                Toast.makeText(this, getString(R.string.no_root_warning), Toast.LENGTH_LONG).show()
            } else {
                startActivity(NotRootActivity.newIntent(this))
            }
        }
    }

    private fun initDagger() {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .suModule(SuModule(this))
                .build()
    }

    companion object {
        lateinit var appComponent: AppComponent
    }
}