package fr.hazegard.drfreeze.model

import android.content.pm.PackageManager

data class PackageApp(
        val pkg: Pkg,
        val appName: String,
        var isDisabled: Boolean,
        var isUninstalled: Boolean) {
    constructor(pkg: Pkg, appName: String) : this(pkg, appName, false, false)

    fun isEnable(pm: PackageManager): Boolean {
        return pm.getApplicationInfo(pkg.s, 0).enabled
    }
}