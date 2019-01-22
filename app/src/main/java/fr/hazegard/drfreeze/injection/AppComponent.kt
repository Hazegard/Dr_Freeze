package fr.hazegard.drfreeze.injection

import dagger.Component
import fr.hazegard.drfreeze.ui.ManageTrackedAppActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, SuModule::class])
interface AppComponent {
    fun inject(manageActivity: ManageTrackedAppActivity)
}