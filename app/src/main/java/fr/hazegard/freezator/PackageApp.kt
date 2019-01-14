package fr.hazegard.freezator

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
import android.support.v4.content.ContextCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.util.Log
import fr.hazegard.freezator.extensions.toBitmap
import fr.hazegard.freezator.ui.ListAppActivity
import fr.hazegard.freezator.ui.ShortcutDispatcherActivity

data class PackageApp(val packageName: String, val appName: String) {

    private val commands by lazy { Commands() }

    /**
     * Disable the package
     */
    fun disable() {
        commands.disablePackage(packageName)
    }

    /**
     * Enable the package
     */
    fun enable() {
        commands.enablePackage(packageName)
    }

    /**
     * Get the icon of package as drawable
     * @param context The current context
     * @return The icon of the package as drawable
     */
    fun getIconDrawable(context: Context): Drawable {
        return context.packageManager.getApplicationIcon(packageName)
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
        NotificationUtils.removeNotification(context, packageName)
    }

    /**
     * Start the package
     * @param context The current context
     */
    fun start(context: Context) {
        enable()
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            ContextCompat.startActivity(context, launchIntent, null)
            notify(context)
        } else {
            Log.d("async", "intent null")
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
                val intent = ShortcutDispatcherActivity.newIntent(context, packageName)
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                val pinShortcutInfo = ShortcutInfo.Builder(context, packageName)
                        .setIcon(Icon.createWithBitmap(getIconBitmap(context)))
                        .setShortLabel(appName)
                        .setIntent(intent)
                        .build()
                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
            }
        } else {
            val shortcutIntent = Intent(context, ListAppActivity::class.java)
            shortcutIntent.action = Intent.ACTION_MAIN
            val addIntent = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, appName)
                putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, getIconBitmap(context))
                action = "com.android.launcher.action.INSTALL_SHORTCUT"
                putExtra("duplicate", false)
            }
            context.sendBroadcast(addIntent)
        }
    }

}