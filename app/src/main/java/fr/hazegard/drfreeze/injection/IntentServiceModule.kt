package fr.hazegard.drfreeze.injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.hazegard.drfreeze.NotificationUtils

@Module
abstract class IntentServiceModule {
    @ContributesAndroidInjector
    abstract fun contributesIntentService(): NotificationUtils.Companion.NotificationActionService
}