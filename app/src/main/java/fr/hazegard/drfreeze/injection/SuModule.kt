package fr.hazegard.drfreeze.injection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import fr.hazegard.drfreeze.Su
import javax.inject.Named
import javax.inject.Singleton


@Module
class SuModule(private val context: Context) {
    @Provides
    @Singleton
    fun providesSu(): Su = Su()

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

//    @Provides
//    @Singleton
//    fun providesSaveHelper(): SaveHelper = SaveHelper()

    @Provides
    @Singleton
    fun providesSystemPackageManager(app: Application): PackageManager = app.packageManager

//    @Provides
//    fun providesPreferencesHelper(app: Application): PreferencesHelper = PreferencesHelper(app)
//    @Provides
//    @Singleton
//    fun providePackageManager(app: Application): fr.hazegard.drfreeze.PackageManager = fr.hazegard.drfreeze.PackageManager(app)
}