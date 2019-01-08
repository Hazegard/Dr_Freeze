package fr.hazegard.freezator.ui

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import fr.hazegard.freezator.AppsManager

class ShortcutReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val packageName: String = intent?.extras?.getString(BUNDLE_PACKAGE_NAME) ?: ""
        Toast.makeText(applicationContext, packageName, Toast.LENGTH_LONG).show()
        val appsManager = AppsManager(applicationContext)
        if ("" != packageName) {
            appsManager.startPackage(packageName, packageManager)
        }
    }

    companion object {
        private const val BUNDLE_PACKAGE_NAME = "BUNDLE_PACKAGE_NAME"
        private const val INTENT_SHORTCUT = "INTENT_SHORTCUT"
        fun newIntent(context: Context, targetPAckageName: String): Intent {
            return Intent(context, ShortcutReceiverActivity::class.java)
                    .setAction(INTENT_SHORTCUT)
                    .putExtra(BUNDLE_PACKAGE_NAME, targetPAckageName)
        }
    }
}
