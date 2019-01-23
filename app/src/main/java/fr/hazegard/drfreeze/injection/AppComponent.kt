package fr.hazegard.drfreeze.injection

import dagger.Component
import dagger.android.AndroidInjectionModule
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.ui.ListPackagesActivity
import fr.hazegard.drfreeze.ui.ManageTrackedAppActivity
import fr.hazegard.drfreeze.ui.NotRootActivity
import fr.hazegard.drfreeze.ui.ShortcutDispatcherActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    SuModule::class,
    BroadcastModule::class,
    IntentServiceModule::class
])
interface AppComponent {
    fun inject(manageActivity: ManageTrackedAppActivity)
    fun inject(freezeApplication: FreezeApplication)
    fun inject(shortcutDispatcherActivity: ShortcutDispatcherActivity)
    fun inject(listPackagesActivity: ListPackagesActivity)
    fun inject(notRootActivity: NotRootActivity)
}