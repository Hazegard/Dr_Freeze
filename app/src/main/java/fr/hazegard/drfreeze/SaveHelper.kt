package fr.hazegard.drfreeze

import android.content.SharedPreferences
import fr.hazegard.drfreeze.model.Pkg
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by Hazegard on 05/03/18.
 */

/**
 * This class manage the saved values in the sharedPreferences
 */
class SaveHelper @Inject constructor(
        @Named("Tracked_Apps_preferences") val trackedAppsPreferences: SharedPreferences) {
//    @Inject
//    @Named("Tracked_Apps_preferences")
//    lateinit var trackedAppsPreferences: SharedPreferences

    /**
     * Save the list of tracked pkg
     * @param pkg List<String> The list of pkg to save
     */
    fun saveTrackedPackages(pkg: List<Pkg>) {
        val set = pkg.map { it.s }.toSet()
//        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
        trackedAppsPreferences
                .edit().apply {
                    clear()
                    putStringSet(LIST, set)
                    apply()
                }
    }

    /**
     * Remove a package from the tracking list
     * @param pkg The package to remove
     */
    fun removeTrackedPackage(pkg: Pkg) {
//        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
        trackedAppsPreferences
                .apply {
                    val packages = getStringSet(LIST, emptySet()) ?: emptySet()
                    packages.remove(pkg.s)
                    edit().apply {
                        clear()
                        putStringSet(LIST, packages)
                        apply()
                    }
                }
    }

    /**
     * Remove a package from the tracking list
     * @param pkg The package to remove
     */
    fun saveTrackedPackage(pkg: Pkg) {
//        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
        trackedAppsPreferences
                .apply {
                    val packages = getStringSet(LIST, emptySet()) ?: emptySet()
                    packages.add(pkg.s)
                    edit().apply {
                        clear()
                        putStringSet(LIST, packages)
                        apply()
                    }
                }
    }

    /**
     * Save the list of tracked packages
     * @param packages Set<String> The set of packages to save
     */
    fun saveTrackedPackages(packages: Set<Pkg>) {
//        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
        trackedAppsPreferences
                .edit().apply {
                    clear()
                    putStringSet(LIST, packages.map { it.s }.toSet())
                    apply()
                }
    }

    /**
     * get the list of tracked packages
     * @return Set<String> The set of saved tracked packages
     */
    fun getTrackedPackages(): MutableSet<Pkg> {
//        return context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
        return trackedAppsPreferences
                .getStringSet(LIST, emptySet())?.map { Pkg(it) }?.toMutableSet()
                ?: Collections.emptySet()
    }

    companion object {
        private const val TRACKED_APPLICATION = "TRACKED_APPLICATION"
        private const val LIST = "LIST"
    }
}