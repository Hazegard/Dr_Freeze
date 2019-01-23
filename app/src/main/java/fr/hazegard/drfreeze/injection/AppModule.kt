package fr.hazegard.drfreeze.injection

import android.app.Application
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule constructor(private val application: Application) {
    @Provides
    @Singleton
    fun providesApplication(): Application = application
}