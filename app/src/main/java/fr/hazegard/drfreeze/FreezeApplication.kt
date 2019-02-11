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
import fr.hazegard.drfreeze.injection.*
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

    @Inject
    lateinit var su: Su

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
    }

    private fun initDagger() {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .systemModule(SystemModule(this))
                .dbModule(DbModule(this))
                .build()
    }

    companion object {
        lateinit var appComponent: AppComponent
    }
}