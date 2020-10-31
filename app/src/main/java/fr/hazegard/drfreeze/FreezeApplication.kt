package fr.hazegard.drfreeze

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import fr.hazegard.drfreeze.injection.*
import javax.inject.Inject

class FreezeApplication : Application(), HasAndroidInjector {
    lateinit var androidInjector: AndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any>? {
        return androidInjector
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