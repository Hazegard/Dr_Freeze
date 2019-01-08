package fr.hazegard.freezator

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log

/**
 * Created by maxime on 01/03/18.
 */
class AppsManager(private var context: Context) {
    private val commands:
            Commands by lazy {
        Commands()
    }
    val packages by lazy { listPackages() }
    val installedPackages by lazy { listInstalledPackages() }

    private fun listPackages(): List<ApplicationInfo> {
        val packages: List<ApplicationInfo> = context.packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA)
                .distinctBy { it.processName }
        Log.d("AppsManager", "Packages : " + packages.size)
        return packages
    }

    private fun listInstalledPackages(): List<ApplicationInfo> {
        val installedPackages = listPackages().filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }
        Log.d("AppsManager", "Packages : " + installedPackages.size)
        return installedPackages
    }

    fun listDisabledApp(): List<String> {
        return commands.listDisabledPackages()
    }

    fun listDisabledPackages(): List<ApplicationInfo> {
        val packagesName = commands.listDisabledPackages()
        return getPackagesListFromPackageNames(packagesName)
    }

    private fun getPackagesListFromPackageNames(packageNames: List<String>): List<ApplicationInfo> {
        return packages.filter {
            packageNames.contains(it.processName)
        }.sortedBy {
            it.processName
        }
    }

    fun enablePackage(packageName: String): String {
        return commands.enablePackage(packageName).trim()
    }

    fun disablePackage(packageName: String): String {
        return commands.disablePackage(packageName).trim()
    }

    fun getEnabledAndMonitored(): List<String> {
        val sp = SharedPreferenceHelper(context)
        val watchedApplications: List<String> = sp.getListMonitoredApplication()
        val disabledApps: List<String> = listDisabledApp()
        return watchedApplications.minus(disabledApps).toList()
    }

}