package fr.hazegard.drfreeze.injection

import dagger.Component
import dagger.android.AndroidInjectionModule
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.ui.*
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    DbModule::class,
    SystemModule::class,
    BroadcastModule::class,
    IntentServiceModule::class
])
interface AppComponent {
    fun inject(splashScreenActivity: SplashScreenActivity)
    fun inject(manageActivity: ManageTrackedAppActivity)
    fun inject(freezeApplication: FreezeApplication)
    fun inject(shortcutDispatcherActivity: ShortcutDispatcherActivity)
    fun inject(listPackagesActivity: ListPackagesActivity)
    fun inject(notRootActivity: NotRootActivity)
    fun inject(settingsActivity: SettingsActivity)
}