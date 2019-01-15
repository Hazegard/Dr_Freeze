package fr.hazegard.freezator

import fr.hazegard.freezator.model.Pkg

class Commands {
    private val su = Su.instance

    /**
     * Enable the package
     * @param pkg The package to enable
     */
    fun enablePackage(pkg: Pkg): String {
        return su.exec("pm enable $pkg")
    }

    /**
     * Disable the package
     * @param pkg The package to disable
     */
    fun disablePackage(pkg: Pkg): String {
        return su.exec("pm disable ${pkg.s}")
    }

    /**
     * List all disable packages
     * @return The list of disabled packages
     */
    fun listDisabledPackages(): List<Pkg> {
        return su.exec("pm list packages -d | cut -d ':' -f 2")
                .split("\n")
                .map {
                    Pkg(it)
                }
    }
}