package fr.hazegard.freezator

import android.content.Context
import java.util.*

/**
 * Created by Hazegard on 05/03/18.
 */

/**
 * This class manage the saved values in the sharedPreferences
 */
class SaveHelper(private val context: Context) {

    /**
     * Save the list of tracked packages
     * @param packages List<String> The list of packages to save
     */
    fun saveTrackedPackages(packages: List<String>) {
        val set = packages.toSet()
        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
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
    fun removeTrackedPackage(pkg: String) {
        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
                .apply {
                    val packages = getStringSet(LIST, emptySet()) ?: emptySet()
                    packages.remove(pkg)
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
    fun saveTrackedpackage(pkg: String) {
        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
                .apply {
                    val packages = getStringSet(LIST, emptySet()) ?: emptySet()
                    packages.add(pkg)
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
    fun saveTrackedPackages(packages: Set<String>) {
        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
                .edit().apply {
                    clear()
                    putStringSet(LIST, packages)
                    apply()
                }
    }

    /**
     * get the list of tracked packages
     * @return Set<String> The set of saved tracked packages
     */
    fun getTrackedPackages(): MutableSet<String> {
        return context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
                .getStringSet(LIST, emptySet()) ?: Collections.emptySet()
    }

    companion object {
        private const val TRACKED_APPLICATION = "TRACKED_APPLICATION"
        private const val LIST = "LIST"
    }
}