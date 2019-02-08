package fr.hazegard.drfreeze

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutManagerCompat
import fr.hazegard.drfreeze.extensions.toBitmap
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import fr.hazegard.drfreeze.ui.ShortcutDispatcherActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageUtils @Inject constructor(
        private val commands: Commands,
        private val pm: PackageManager,
        private val imageManager: ImageManager,
        private val notificationManager: NotificationManager) {
    /**
     * Disable the package
     * @param pkg The package to disable
     */
    fun disablePackage(pkg: Pkg): String {
        notificationManager.removeNotification(pkg)
        return commands.disablePackage(pkg).trim()
    }

    /**
     * Enable the package
     * @param pkg The package to enable
     */
    fun enablePackage(pkg: Pkg): String {
        return commands.enablePackage(pkg).trim()
    }

    /**
     * Get the application of a package
     * @param pkg The package of the application name to fetch
     * @return The application name
     */
    fun getAppName(pkg: Pkg): String {
        val appInfo = pm.getApplicationInfo(pkg.s,
                PackageManager.GET_META_DATA)
        return pm.getApplicationLabel(appInfo).toString()
    }


    fun safeCreatePackageApp(pkg: Pkg): PackageApp? {
        return try {
            PackageApp(pkg, getAppName(pkg))
        } catch (e: PackageManager.NameNotFoundException) {
            PackageApp(pkg, "Uninstalled")
        }
    }

    fun isPackageEnabled(pkg: Pkg): Boolean {
        return try {
            pm.getApplicationInfo(pkg.s, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isPackageInstalled(pkg: Pkg): Boolean {
        return try {
            pm.getPackageInfo(pkg.s, 0) != null
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Start the package
     * @param context The current context
     */
    fun start(pkg: PackageApp, context: Context) {
        enablePackage(pkg.pkg)
        val launchIntent = pm.getLaunchIntentForPackage(pkg.pkg.s)
        if (launchIntent != null) {
            ContextCompat.startActivity(context, launchIntent, null)
            notificationManager.sendNotification(pkg)
        } else {
            Log.d("PackageApp", "Unable to start ${pkg.appName} (Launch intent fo package: ${pkg.pkg.s} is null)")
        }
    }


    /**
     * Add a shortcut leading to ShortcutDispatcherActivity
     * With an intent containing the package
     * @param context The current context
     */
    fun addShortcut(context: Context, packageApp: PackageApp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                val intent = ShortcutDispatcherActivity.newIntent(context, packageApp.pkg)
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                val pinShortcutInfo = ShortcutInfo.Builder(context, packageApp.pkg.s)
                        .setIcon(Icon.createWithBitmap(imageManager.getCachedImage(packageApp).toBitmap()))
                        .setShortLabel(packageApp.appName)
                        .setIntent(intent)
                        .build()
                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
            }
        } else {
            val shortcutIntent = ShortcutDispatcherActivity.newIntent(context, packageApp.pkg)
            shortcutIntent.action = Intent.ACTION_MAIN
            val icon = Bitmap.createScaledBitmap(imageManager.getCachedImage(packageApp).toBitmap(), 128, 128, true)
            @Suppress("DEPRECATION") val addIntent = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, packageApp.appName)
                putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
                action = INSTALL_ACTION
                putExtra("duplicate", false)
            }
            context.sendBroadcast(addIntent)
        }
    }

    companion object {
        private const val INSTALL_ACTION = "com.android.launcher.action.INSTALL_SHORTCUT"
    }
}