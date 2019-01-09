package fr.hazegard.freezator.ui

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import fr.hazegard.freezator.AppsManager

class ShortcutBroadcastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val packageName: String = intent?.extras?.getString(BUNDLE_PACKAGE_NAME) ?: ""
        Toast.makeText(this@ShortcutBroadcastActivity, packageName, Toast.LENGTH_LONG).show()
        val appsManager = AppsManager(this@ShortcutBroadcastActivity)
        if ("" != packageName) {
            appsManager.startPackage(packageName, this@ShortcutBroadcastActivity)
        } else {
            Toast.makeText(this@ShortcutBroadcastActivity,
                    "Application not found, it may have been uninstalled", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    companion object {
        private const val BUNDLE_PACKAGE_NAME = "BUNDLE_PACKAGE_NAME"
        private const val INTENT_SHORTCUT = "INTENT_SHORTCUT"
        fun newIntent(context: Context, targetPAckageName: String): Intent {
            return Intent(context, ShortcutBroadcastActivity::class.java)
                    .setAction(INTENT_SHORTCUT)
                    .putExtra(BUNDLE_PACKAGE_NAME, targetPAckageName)
        }
    }
}
