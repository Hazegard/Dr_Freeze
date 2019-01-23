package fr.hazegard.drfreeze.injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.hazegard.drfreeze.OnBootService

@Module
abstract class BroadcastModule {
    @ContributesAndroidInjector
    abstract fun contributesBroadcast(): OnBootService
}