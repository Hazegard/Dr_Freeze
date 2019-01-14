package fr.hazegard.freezator.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import fr.hazegard.freezator.PackageApp
import fr.hazegard.freezator.PackageManager

/**
 * This activity is used to dispatch the intent coming from the shortcut
 * It is used as the shortcut must intent to an activity
 * It has a invisible theme so the user won't notice it
 */
class ShortcutDispatcherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val packageName: String = intent?.extras?.getString(BUNDLE_PACKAGE_NAME) ?: ""
        val pkg = PackageApp(packageName, PackageManager.getAppName(this, packageName))
        if ("" != packageName) {
            pkg.start(this)
        } else {
            Toast.makeText(this, "Application not found, it may have been uninstalled",
                    Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    companion object {
        private const val BUNDLE_PACKAGE_NAME = "BUNDLE_PACKAGE_NAME"
        private const val INTENT_SHORTCUT = "INTENT_SHORTCUT"
        fun newIntent(context: Context, targetPackageName: String): Intent {
            return Intent(context, ShortcutDispatcherActivity::class.java)
                    .setAction(INTENT_SHORTCUT)
                    .putExtra(BUNDLE_PACKAGE_NAME, targetPackageName)
        }
    }
}
