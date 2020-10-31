package fr.hazegard.drfreeze.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.PackageUtils
import fr.hazegard.drfreeze.R
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
    lateinit var packageUtils: PackageUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        FreezeApplication.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        val packageName: String = intent?.extras?.getString(BUNDLE_PACKAGE_NAME) ?: ""
        if ("" == packageName) {
            Toast.makeText(
                    this,
                    getString(R.string.application_not_found),
                    Toast.LENGTH_SHORT
            ).show()
        } else {
            try {
                val pkg = Pkg(packageName)
                val targetPackage = PackageApp(pkg, packageUtils.getAppName(pkg))
                packageUtils.start(targetPackage, this)
            } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                Toast.makeText(
                        this,
                        getString(R.string.application_not_found),
                        Toast.LENGTH_SHORT
                ).show()
            }
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
