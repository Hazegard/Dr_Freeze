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
        private val notificationUtils: NotificationUtils) {

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

    /**
     * Start the package
     * @param context The current context
     */
    fun start(pkg: PackageApp, context: Context) {
        enablePackage(pkg.pkg)
        val launchIntent = pm.getLaunchIntentForPackage(pkg.pkg.s)
        if (launchIntent != null) {
            ContextCompat.startActivity(context, launchIntent, null)
            notificationUtils.sendNotification(pkg)
        } else {
            Log.d("PackageApp", "Unable to start ${pkg.appName} (Launch intent fo package: ${pkg.pkg.s} is null)")
        }
    }

    /**
     * Add a shortcut leading to ShortcutDispatcherActivity
     * With an intent containing the package
     * @param context The current context
     */
    fun addShortcut(context: Context, packageApp: PackageApp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                val intent = ShortcutDispatcherActivity.newIntent(context, packageApp.pkg)
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                val pinShortcutInfo = ShortcutInfo.Builder(context, packageApp.pkg.s)
                        .setIcon(Icon.createWithBitmap(packageApp.getIconBitmap(pm)))
                        .setShortLabel(packageApp.appName)
                        .setIntent(intent)
                        .build()
                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
            }
        } else {
            val shortcutIntent = ShortcutDispatcherActivity.newIntent(context, packageApp.pkg)
            shortcutIntent.action = Intent.ACTION_MAIN
            val icon = Bitmap.createScaledBitmap(packageApp.getIconBitmap(pm), 128, 128, true)
            @Suppress("DEPRECATION") val addIntent = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, packageApp.appName)
                putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
                action = "com.android.launcher.action.INSTALL_SHORTCUT"
                putExtra("duplicate", false)
            }
            context.sendBroadcast(addIntent)
        }
    }
}