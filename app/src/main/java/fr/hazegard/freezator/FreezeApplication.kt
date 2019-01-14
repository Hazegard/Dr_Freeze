package fr.hazegard.freezator

import android.app.Application
import android.support.v7.app.AppCompatDelegate

class FreezeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
}