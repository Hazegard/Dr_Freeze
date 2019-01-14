package fr.hazegard.freezator

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/**
 * Created by Hazegard on 01/03/18.
 */
class PackageManager(private var context: Context) {
    private val commands: Commands by lazy {
        Commands()
    }

    private val saveHelper: SaveHelper by lazy { SaveHelper(context) }

    /**
     * Get a list of all packages (including system packages)
     * @return a list of all packages
     */
    private fun getAllPackages(): List<ApplicationInfo> {
        return context.packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA)
                .distinctBy { it.processName }
                .sortedBy { it.loadLabel(context.packageManager).toString() }
    }

    /**
     * Get a list of packages installed by the user
     * @return the list of packages installed by the user
     */
    private fun getInstalledPackages(): List<ApplicationInfo> {
        return getAllPackages().filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }
    }

    /**
     * Get a list of packages
     * Depending on the preference set by the user
     * @return The list of package (All packages or Installed packages)
     */
    fun getPackages(): List<PackageApp> {
        return (if (PreferencesHelper.isSystemAppsEnabled(context)) {
            getAllPackages()
        } else {
            getInstalledPackages()
        }).map {
            PackageApp(it.packageName, getAppName(context, it.packageName))
        }
    }

    /**
     * Get a set of enabled packages
     * @return The set of enabled packages
     */
    fun getTrackedPackagesAsSet(): MutableSet<String> {
        return saveHelper.getTrackedPackages()
    }

    /**
     * Get a list of disabled packages
     * @return The list of disabled packages
     */
    fun getDisabledPackages(): List<String> {
        return commands.listDisabledPackages()
    }

    /**
     * Get a list of enabled and tracked packages
     * @return The list of enabled and tracked packages
     */
    fun getEnabledAndTracked(): List<PackageApp> {
        val trackedApplications: List<String> = this.getTrackedPackagesAsList()
        val disabledApps: List<String> = getDisabledPackages()
        return trackedApplications.minus(disabledApps).toList().map {
            PackageApp(it, getAppName(context, it))
        }
    }

    /**
     * Get a list of tracked packages
     * @return THe list of tracked packages
     */
    private fun getTrackedPackagesAsList(): List<String> {
        return saveHelper.getTrackedPackages()
                .sortedBy { it }
                .toList()
    }

    /**
     * Get a list of tracked packages
     * @return THe list of tracked packages
     */
    fun getTrackedPackages(): List<PackageApp> {
        return saveHelper.getTrackedPackages()
                .sortedBy { it }
                .toList().map {
                    PackageApp(it, getAppName(context, it))
                }
    }

    /**
     * Save a list of tracked packages
     * @param packages The list of packages to save
     */
    fun saveTrackedPackages(packages: List<String>) {
        saveHelper.saveTrackedPackages(packages)
    }

    /**
     * Save a set of tracked packages
     * @param packages The set of packages to save
     */
    fun saveTrackedPackages(packages: Set<String>) {
        saveHelper.saveTrackedPackages(packages)
    }

    /**
     * Untrack the package
     * @param pkg The package to untrack
     */
    fun removeTrackedPackage(pkg: PackageApp) {
        saveHelper.removeTrackedPackage(pkg.packageName)
        pkg.enable()
    }

    /**
     * Track the package
     * @param pkg The package to untrack
     */
    fun addTrackedPackage(pkg: PackageApp) {
        saveHelper.saveTrackedpackage(pkg.packageName)
    }

    /**
     * Disable the package
     * @param pkg The package to disable
     */
    fun disablePackage(pkg: String): String {
        return commands.disablePackage(pkg).trim()
    }

    companion object {
        /**
         * Get the application of a package
         * @param context The current context
         * @param packageName The package of the application name to fetch
         * @return The application name
         */
        fun getAppName(context: Context, packageName: String): String {
            val appInfo = context.packageManager.getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA)
            return context.packageManager.getApplicationLabel(appInfo).toString()
        }
    }

}