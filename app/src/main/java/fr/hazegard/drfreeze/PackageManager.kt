package fr.hazegard.drfreeze

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutManagerCompat
import fr.hazegard.drfreeze.extensions.isLaunchableApp
import fr.hazegard.drfreeze.extensions.isSystemApp
import fr.hazegard.drfreeze.extensions.toBitmap
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import fr.hazegard.drfreeze.ui.ShortcutDispatcherActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Hazegard on 01/03/18.
 */
@Singleton
class PackageManager @Inject constructor(
        private val preferencesHelper: PreferencesHelper,
        private val commands: Commands,
        private val pm: PackageManager,
        private val saveHelper: SaveHelper,
        private val imageManager: ImageManager,
        private val packageUtils: PackageUtils) {

    /**
     * Get a list of all packages (including system packages)
     * @return a list of all packages
     */
    private fun getAllPackages(): List<ApplicationInfo> {
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .distinctBy { it.processName }
    }

    /**
     * Get a list of packages installed by the user
     * @return the list of packages installed by the user
     */
    private fun getInstalledPackages(): List<ApplicationInfo> {
        return getAllPackages().filter {
            !it.isSystemApp()
        }
    }

    /**
     * Get a list of packages
     * Depending on the preference set by the user
     * @return The list of package (All packages or Installed packages)
     */
    fun getPackages(): List<PackageApp> {
        val doKeepSystemApps = preferencesHelper.isSystemAppsEnabled()
        val showOnlyLaunchApps = preferencesHelper.isOnlyLauncherApp()
        return getAllPackages()
                .filter { doKeepSystemApps || it.isSystemApp() }
                .filter { !showOnlyLaunchApps || pm.isLaunchableApp(Pkg(it.packageName)) }
                .mapNotNull {
                    val pkg = Pkg(it.packageName)
                    return@mapNotNull packageUtils.safeCreatePackageApp(pkg)
                }
                .sortedBy { it.appName }
    }

    /**
     * Get a set of enabled packages
     * @return The set of enabled packages
     */
    fun getTrackedPackagesAsSet(): MutableSet<Pkg> {
        return saveHelper.getTrackedPackages()
    }

    /**
     * Get a list of disabled packages
     * @return The list of disabled packages
     */
    fun getDisabledPackages(): List<Pkg> {
        return commands.listDisabledPackages()
    }

    /**
     * Get a list of enabled and tracked packages
     * @return The list of enabled and tracked packages
     */
    fun getEnabledAndTracked(): List<PackageApp> {
        val trackedApplications: List<Pkg> = getTrackedPackagesAsList()
        val disabledApps: List<Pkg> = getDisabledPackages()
        return trackedApplications.minus(disabledApps).toList().mapNotNull {
            packageUtils.safeCreatePackageApp(it)
        }
    }

    /**
     * Get a list of tracked packages
     * @return THe list of tracked packages
     */
    private fun getTrackedPackagesAsList(): List<Pkg> {
        return saveHelper.getTrackedPackages()
                .sortedBy { it.s }
                .toList()
    }

    /**
     * Get a list of tracked packages
     * @return THe list of tracked packages
     */
    fun getTrackedPackages(): List<PackageApp> {
        return saveHelper.getTrackedPackages()
                .toList().mapNotNull {
                    packageUtils.safeCreatePackageApp(it)
                }
                .sortedBy { it.appName }
    }

    /**
     * Save a list of tracked packages
     * @param packages The list of packages to save
     */
    fun saveTrackedPackages(packages: List<Pkg>) {
        saveHelper.saveTrackedPackages(packages)
    }

    /**
     * Save a set of tracked packages
     * @param packages The set of packages to save
     */
    fun saveTrackedPackages(packages: Set<Pkg>) {
        saveHelper.saveTrackedPackages(packages)
    }

    /**
     * Untrack the package
     * @param pkg The package to untrack
     */
    fun removeTrackedPackage(pkg: PackageApp) {
        saveHelper.removeTrackedPackage(pkg.pkg)
        imageManager.deleteImage(pkg)
        packageUtils.enablePackage(pkg.pkg)
    }

    /**
     * Track the package
     * @param pkg The package to track
     */
    fun addTrackedPackage(pkg: Pkg) {
        saveHelper.saveTrackedPackage(pkg)
    }
}