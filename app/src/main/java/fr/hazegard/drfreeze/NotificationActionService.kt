package fr.hazegard.drfreeze

import android.app.IntentService
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import fr.hazegard.drfreeze.model.Pkg
import javax.inject.Inject

/**
 * The class that handle intent send by notifications
 * The intent actions are:
 *  - ACTION_DISABLE: Disable the package given as extra
 */
class NotificationActionService : IntentService("NotificationActionService") {
    @Inject
    lateinit var packageUtils: PackageUtils

    override fun onHandleIntent(intent: Intent?) {
        AndroidInjection.inject(this)
        val action = intent?.action
        if (action.equals(ACTION_DISABLE)) {
            intent?.extras?.getString(KEY_PACKAGE, null)?.let {
                packageUtils.disablePackage(Pkg(it))
            }
        }
    }

    companion object {
        private const val ACTION_DISABLE = "ACTION_DISABLE"
        private const val KEY_PACKAGE = "KAY_PACKAGE"
        /**
         * Create a new Intent that disable the package
         * @param context The current context
         * @param pkg The package that should be disabled
         */
        fun newDisablePackageIntent(context: Context, pkg: Pkg): Intent {
            return Intent(context, NotificationActionService::class.java).apply {
                action = ACTION_DISABLE
                putExtra(KEY_PACKAGE, pkg.s)
            }
        }
    }
}