package fr.hazegard.drfreeze.extensions

import android.content.Intent
import android.content.pm.PackageManager
import fr.hazegard.drfreeze.model.Pkg

/**
 * Return whether can be launch via a launcher
 * @return Is the app can be launcher
 */
fun PackageManager.isLaunchableApp(pkg: Pkg): Boolean {
    return this.getLaunchIntentForPackage(pkg.s)
            ?.categories
            ?.contains(Intent.CATEGORY_LAUNCHER)
            ?: false
}