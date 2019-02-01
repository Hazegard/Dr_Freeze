package fr.hazegard.drfreeze.model

import android.content.pm.PackageManager

data class PackageApp(val pkg: Pkg, val appName: String) {

    fun isEnable(pm: PackageManager): Boolean {
        return pm.getApplicationInfo(pkg.s, 0).enabled
    }
}