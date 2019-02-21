package fr.hazegard.drfreeze

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import fr.hazegard.drfreeze.extensions.isLaunchableApp
import fr.hazegard.drfreeze.extensions.isSystemApp
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import fr.hazegard.drfreeze.repository.DbWrapper
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
        private val dbWrapper: DbWrapper,
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
                .map {
                    val pkg = Pkg(it.packageName)
                    return@map packageUtils.safeCreatePackageApp(pkg)
                }
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.appName })
    }

    /**
     * Get a set of enabled packages
     * @return The set of enabled packages
     */
    fun getTrackedPackagesAsMap(): MutableMap<Pkg, PackageApp> {
        return getTrackedPackages().fold(mutableMapOf()) { acc, curr ->
            acc[curr.pkg] = curr
            return@fold acc
        }
    }

    /**
     * Get a list of disabled packages
     * @return The list of disabled packages
     */
    private fun getDisabledPackages(): List<Pkg> {
        return commands.listDisabledPackages()
    }

    /**
     * Get a list of enabled and tracked packages
     * @return The list of enabled and tracked packages
     */
    fun getEnabledInstalledAndTracked(): List<PackageApp> {
        val trackedApplications: List<PackageApp> = dbWrapper.selectPackagesToNotify()
        val disabledApps: List<Pkg> = getDisabledPackages()
        val installedApps: List<Pkg> = getInstalledPackages().map { Pkg(it.packageName) }
        return trackedApplications.filter {
            !disabledApps.contains(it.pkg) && !installedApps.contains(it.pkg)
        }
    }

    /**
     * Get a list of enabled and tracked packages
     * @return The list of enabled and tracked packages
     */
    fun getDisabledInstalledAndTracked(): List<PackageApp> {
        val trackedApplications: List<PackageApp> = dbWrapper.selectPackagesToNotify()
        val disabledApps: List<Pkg> = getDisabledPackages()
        val installedApps: List<Pkg> = getInstalledPackages().map { Pkg(it.packageName) }
        return trackedApplications.filter {
            disabledApps.contains(it.pkg) // && installedApps.contains(it.pkg)
        }
    }

    /**
     * Get a list of tracked packages
     * @return THe list of tracked packages
     */
    private fun getTrackedPackagesAsList(): List<PackageApp> {
        return dbWrapper.getAllPackages()
    }

    /**
     * Get a list of tracked packages
     * @return THe list of tracked packages
     */
    fun getTrackedPackages(): List<PackageApp> {
        return dbWrapper.getAllPackages()
    }

    /**
     * Save a list of tracked packages
     * @param packages The list of packages to save
     */
    private fun saveTrackedPackages(packages: List<PackageApp>) {
        //TODO Optimize multiple insertions
        packages.forEach {
            dbWrapper.insertOrUpdateOne(it)
        }
    }

    /**
     * Save a set of tracked packages
     * @param packages The set of packages to save
     */
    fun saveTrackedPackages(packages: Map<Pkg, PackageApp>) {
        //TODO Optimize multiple insertions
        packages.values.forEach {
            dbWrapper.insertOrUpdateOne(it)
        }
    }

    /**
     * Untrack the package
     * @param pkg The package to untrack
     */
    fun removeTrackedPackage(pkg: PackageApp) {
        dbWrapper.deletePackage(pkg)
        imageManager.deleteImage(pkg)
        packageUtils.enablePackage(pkg.pkg)
    }

    /**
     * Untrack the list of package
     * @param packages The list of package to untrack
     */
    private fun removeTrackedPackages(packages: List<PackageApp>) {
        packages.forEach {
            removeTrackedPackage(it)
        }
    }

    fun removeTrackedPackages(packages: Map<Pkg, PackageApp>) {
        packages.values.forEach {
            removeTrackedPackage(it)
        }
    }

    fun updateTrackedPackages(packagesToAdd: List<PackageApp>?, packagesToRemove: List<PackageApp>?) {
        packagesToAdd?.let {
            saveTrackedPackages(it)
        }
        packagesToRemove?.let {
            removeTrackedPackages(it)
        }
    }

    /**
     * Update in the database th notification status
     */
    fun updateNotification(packageApp: PackageApp) {
        dbWrapper.updateNotificationStatus(packageApp)
    }

    /**
     * Get whether the package must be notified
     */
    fun getNotificationStatus(pkg: Pkg): Boolean {
        return dbWrapper.getNotificationStatus(pkg)
    }

    /**
     * Track the package
     * @param pkg The package to track
     */
    fun addTrackedPackage(pkg: PackageApp) {
        dbWrapper.insertOrUpdateOne(pkg)
    }
}