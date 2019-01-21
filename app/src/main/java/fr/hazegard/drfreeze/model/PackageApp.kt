package fr.hazegard.drfreeze.model

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutManagerCompat
import android.util.Log
import fr.hazegard.drfreeze.Commands
import fr.hazegard.drfreeze.NotificationUtils
import fr.hazegard.drfreeze.extensions.toBitmap
import fr.hazegard.drfreeze.ui.ShortcutDispatcherActivity

data class PackageApp(val pkg: Pkg, val appName: String) {

    private val commands by lazy { Commands() }

    /**
     * Disable the package
     */
    fun disable() {
        commands.disablePackage(pkg)
    }

    /**
     * Enable the package
     */
    fun enable() {
        commands.enablePackage(pkg)
    }

    fun isEnable(context: Context): Boolean {
        return context.packageManager.getApplicationInfo(pkg.s, 0).enabled
    }

    /**
     * Get the icon of package as drawable
     * @param context The current context
     * @return The icon of the package as drawable
     */
    fun getIconDrawable(context: Context): Drawable {
        return context.packageManager.getApplicationIcon(pkg.s)
                ?: ColorDrawable(Color.TRANSPARENT)
    }

    /**
     * Get the icon a a package as bitmap
     * @param context The current context
     * @return The icon of the package as bitmap
     */
    fun getIconBitmap(context: Context): Bitmap {
        return getIconDrawable(context).toBitmap()
    }

    /**
     * Send a notification displaying that the package is currently running
     * Allowing the user to disable the package by clicking on the notification
     * @param context The current context
     */
    fun notify(context: Context) {
        NotificationUtils.sendNotification(context, this)
    }

    /**
     * Remove a notification
     * @param context The current context
     */
    fun removeNotification(context: Context) {
        NotificationUtils.removeNotification(context, pkg)
    }

    /**
     * Start the package
     * @param context The current context
     */
    fun start(context: Context) {
        enable()
        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg.s)
        if (launchIntent != null) {
            ContextCompat.startActivity(context, launchIntent, null)
            notify(context)
        } else {
            Log.d("PackageApp", "Unable to start ${this.appName} (Launch intent fo package: ${pkg.s} is null)")
        }
    }

    /**
     * Add a shortcut leading to ShortcutDispatcherActivity
     * With an intent containing the package
     * @param context The current context
     */
    fun addShortcut(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                val intent = ShortcutDispatcherActivity.newIntent(context, pkg)
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                val pinShortcutInfo = ShortcutInfo.Builder(context, pkg.s)
                        .setIcon(Icon.createWithBitmap(getIconBitmap(context)))
                        .setShortLabel(appName)
                        .setIntent(intent)
                        .build()
                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
            }
        } else {
            val shortcutIntent = ShortcutDispatcherActivity.newIntent(context, pkg)
            shortcutIntent.action = Intent.ACTION_MAIN
            val icon = Bitmap.createScaledBitmap(getIconBitmap(context), 128, 128, true)
            @Suppress("DEPRECATION") val addIntent = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, appName)
                putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
                action = "com.android.launcher.action.INSTALL_SHORTCUT"
                putExtra("duplicate", false)
            }
            context.sendBroadcast(addIntent)
        }
    }
}