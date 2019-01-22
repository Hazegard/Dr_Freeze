package fr.hazegard.drfreeze

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import fr.hazegard.drfreeze.extensions.isLaunchableApp
import fr.hazegard.drfreeze.extensions.isSystemApp
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Hazegard on 01/03/18.
 */
class PackageManager @Inject constructor(
        private val preferencesHelper: PreferencesHelper,
        val commands: Commands,
        val pm: PackageManager,
        val saveHelper: SaveHelper/*private var context: Context*/) {
//    private val commands: Commands by lazy {
//        Commands()
//    }

//    private val preferencesHelper: PreferencesHelper by lazy {
//        PreferencesHelper(context)
//    }

//    @Inject
//    lateinit var saveHelper: SaveHelper

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
                .filter { !showOnlyLaunchApps || it.isLaunchableApp(pm) }
                .map {
                    val pkg = Pkg(it.packageName)
                    return@map PackageApp(pkg, getAppName(pkg))
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
        return trackedApplications.minus(disabledApps).toList().map {
            PackageApp(it, getAppName(it))
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
                .toList().map {
                    PackageApp(it, getAppName(it))
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
        enablePackage(pkg.pkg)
    }

    /**
     * Track the package
     * @param pkg The package to track
     */
    fun addTrackedPackage(pkg: Pkg) {
        saveHelper.saveTrackedPackage(pkg)
    }

    /**
     * Disable the package
     * @param pkg The package to disable
     */
    fun disablePackage(pkg: Pkg): String {
        return commands.disablePackage(pkg).trim()
    }

    /**
     * Enable the package
     * @param pkg The package to enable
     */
    fun enablePackage(pkg: Pkg): String {
        return commands.enablePackage(pkg).trim()
    }

    /**
     * Get the application of a package
     * @param context The current context
     * @param pkg The package of the application name to fetch
     * @return The application name
     */
    fun getAppName(pkg: Pkg): String {
        val appInfo = pm.getApplicationInfo(pkg.s,
                PackageManager.GET_META_DATA)
        return pm.getApplicationLabel(appInfo).toString()
    }

    companion object {
        /**
         * Get the application of a package
         * @param context The current context
         * @param pkg The package of the application name to fetch
         * @return The application name
         */
        fun getAppName(context: Context, pkg: Pkg): String {
            val appInfo = context.packageManager.getApplicationInfo(pkg.s,
                    PackageManager.GET_META_DATA)
            return context.packageManager.getApplicationLabel(appInfo).toString()
        }
    }
}