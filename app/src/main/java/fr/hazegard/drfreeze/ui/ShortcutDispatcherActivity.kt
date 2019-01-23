package fr.hazegard.drfreeze.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.PackageManager
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import javax.inject.Inject

/**
 * This activity is used to dispatch the intent coming from the shortcut
 * It is used as the shortcut must intent to an activity
 * It has a invisible theme so the user won't notice it
 */
class ShortcutDispatcherActivity : AppCompatActivity() {

    @Inject
    lateinit var packageManager: PackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        FreezeApplication.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        val packageName: String = intent?.extras?.getString(BUNDLE_PACKAGE_NAME) ?: ""
        if ("" == packageName) {
            Toast.makeText(this, "Application not found, it may have been uninstalled",
                    Toast.LENGTH_SHORT).show()
        } else {
            val pkg = Pkg(packageName)
            val targetPackage = PackageApp(pkg, PackageManager.getAppName(this, pkg))
            packageManager.start(targetPackage, this)
        }
        finish()
    }

    companion object {
        private const val BUNDLE_PACKAGE_NAME = "BUNDLE_PACKAGE_NAME"
        private const val INTENT_SHORTCUT = "INTENT_SHORTCUT"
        fun newIntent(context: Context, targetPackage: Pkg): Intent {
            return Intent(context, ShortcutDispatcherActivity::class.java)
                    .setAction(INTENT_SHORTCUT)
                    .putExtra(BUNDLE_PACKAGE_NAME, targetPackage.s)
        }
    }
}
