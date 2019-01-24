package fr.hazegard.drfreeze.model

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import fr.hazegard.drfreeze.extensions.toBitmap

data class PackageApp(val pkg: Pkg, val appName: String) {

    fun isEnable(pm: PackageManager): Boolean {
        return pm.getApplicationInfo(pkg.s, 0).enabled
    }

    /**
     * Get the icon of package as drawable
     * @param context The current context
     * @return The icon of the package as drawable
     */
    fun getIconDrawable(pm: PackageManager): Drawable {
        return pm.getApplicationIcon(pkg.s) ?: ColorDrawable(Color.TRANSPARENT)
    }

    /**
     * Get the icon a a package as bitmap
     * @param context The current context
     * @return The icon of the package as bitmap
     */
    fun getIconBitmap(pm: PackageManager): Bitmap {
        return getIconDrawable(pm).toBitmap()
    }
}