package fr.hazegard.drfreeze.extensions

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/**
 * Return whether the application is a system app or not
 * @return Is the app system app
 */
fun ApplicationInfo.isSystemApp(): Boolean {
    return (this.flags and ApplicationInfo.FLAG_SYSTEM) == 0
}
