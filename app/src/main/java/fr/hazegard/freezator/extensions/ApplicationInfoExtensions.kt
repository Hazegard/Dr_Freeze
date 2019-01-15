package fr.hazegard.freezator.extensions

import android.content.pm.ApplicationInfo

/**
 * Return whether the application is a system app or not
 * @return Is the app system app
 */
fun ApplicationInfo.isSystemApp(): Boolean {
    return (this.flags and ApplicationInfo.FLAG_SYSTEM) == 0
}