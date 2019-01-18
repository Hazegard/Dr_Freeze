package fr.hazegard.drfreeze

import fr.hazegard.drfreeze.exception.NotRootException
import fr.hazegard.drfreeze.model.Pkg
import java.util.*

class Commands {

    /**
     * Enable the package
     * @param pkg The package to enable
     */
    fun enablePackage(pkg: Pkg): String {
        return try {
            Su.instance.exec("pm enable ${pkg.s}")
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
            Su.instance.exec("pm disable ${pkg.s}")
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
            Su.instance.exec("pm list packages -d | cut -d ':' -f 2")
                    .split("\n")
                    .map {
                        Pkg(it)
                    }
        } catch (e: NotRootException) {
            Collections.emptyList()
        }
    }
}