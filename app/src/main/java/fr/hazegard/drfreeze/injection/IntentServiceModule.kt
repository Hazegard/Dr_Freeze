package fr.hazegard.drfreeze.injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fr.hazegard.drfreeze.NotificationActionService
import fr.hazegard.drfreeze.StopBatchUpdateService

@Module
abstract class IntentServiceModule {
    @ContributesAndroidInjector
    abstract fun contributesIntentService(): NotificationActionService

    @ContributesAndroidInjector
    abstract fun contributesIntentBatchService(): StopBatchUpdateService
}