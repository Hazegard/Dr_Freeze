package fr.hazegard.drfreeze

import android.content.Context
import fr.hazegard.drfreeze.model.Pkg
import java.util.*

/**
 * Created by Hazegard on 05/03/18.
 */

/**
 * This class manage the saved values in the sharedPreferences
 */
class SaveHelper(private val context: Context) {

    /**
     * Save the list of tracked pkg
     * @param pkg List<String> The list of pkg to save
     */
    fun saveTrackedPackages(pkg: List<Pkg>) {
        val set = pkg.map { it.s }.toSet()
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
    fun removeTrackedPackage(pkg: Pkg) {
        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
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
        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
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
        context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
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
        return context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
                .getStringSet(LIST, emptySet())?.map { Pkg(it) }?.toMutableSet()
                ?: Collections.emptySet()
    }

    companion object {
        private const val TRACKED_APPLICATION = "TRACKED_APPLICATION"
        private const val LIST = "LIST"
    }
}