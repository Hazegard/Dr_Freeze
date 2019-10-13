package fr.hazegard.drfreeze.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class SystemModule(private val context: Context) {
    @Provides
    @Singleton
    @Named("Shared_preferences")
    fun providesSharedPreferences(app: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app)
    }

    @Provides
    @Singleton
    fun providesContext() = context

    @Provides
    @Singleton
    @Named("Tracked_Apps_preferences")
    fun providesTrackedAppsPreferences(app: Application): SharedPreferences {
        return app.getSharedPreferences("TRACKED_APPLICATION", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesSystemPackageManager(app: Application): PackageManager = app.packageManager
}