package fr.hazegard.drfreeze.injection

import dagger.Module
import dagger.Provides
import fr.hazegard.drfreeze.Su
import javax.inject.Singleton

@Module
class SuModule {
    @Provides
    @Singleton
    fun providesSu(): Su = Su()
}