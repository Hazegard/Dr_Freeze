package fr.hazegard.drfreeze.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import fr.hazegard.drfreeze.repository.DbWrapper
import javax.inject.Singleton

@Module
class DbModule(private val context: Context) {
    @Provides
    @Singleton
    fun providesDbWrapper() = DbWrapper(context)
}