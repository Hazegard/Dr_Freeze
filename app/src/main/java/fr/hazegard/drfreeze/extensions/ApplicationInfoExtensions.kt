package fr.hazegard.drfreeze.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo

/**
 * Return whether the application is a system app or not
 * @return Is the app system app
 */
fun ApplicationInfo.isSystemApp(): Boolean {
    return (this.flags and ApplicationInfo.FLAG_SYSTEM) == 0
}

/**
 * Return whether can be launch via a launcher
 * @return Is the app can be launcher
 */
fun ApplicationInfo.isLaunchableApp(context: Context): Boolean {
    return context.packageManager.getLaunchIntentForPackage(this.packageName)
            ?.categories?.contains(Intent.CATEGORY_LAUNCHER) ?: false
}