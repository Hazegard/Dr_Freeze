package fr.hazegard.drfreeze

import fr.hazegard.drfreeze.exception.NotRootException
import fr.hazegard.drfreeze.model.Pkg
import java.util.*
import javax.inject.Inject

class Commands @Inject constructor(private val su: Su) {

    /**
     * Enable the package
     * @param pkg The package to enable
     */
    fun enablePackage(pkg: Pkg): String {
        return try {
            su.exec("pm enable ${pkg.s}")
        } catch (e: NotRootException) {
            ""
        }
    }

    /**
     * Disable the package
     * @param pkg The package to disable
     */
    fun disablePackage(pkg: Pkg): String {
        return try {
            su.exec("pm disable ${pkg.s}")
        } catch (e: NotRootException) {
            ""
        }
    }

    /**
     * List all disable packages
     * @return The list of disabled packages
     */
    fun listDisabledPackages(): List<Pkg> {
        return try {
            su.exec("pm list packages -d | cut -d ':' -f 2")
                    .split("\n")
                    .map {
                        Pkg(it)
                    }
        } catch (e: NotRootException) {
            Collections.emptyList()
        }
    }
}