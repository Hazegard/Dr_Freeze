package fr.hazegard.freezator

class Commands {
    private val su = Su.instance

    /**
     * Enable the package
     * @param packageName The package to enable
     */
    fun enablePackage(packageName: String): String {
        return su.exec("pm enable $packageName")
    }

    /**
     * Disable the package
     * @param packageName The package to disable
     */
    fun disablePackage(packageName: String): String {
        return su.exec("pm disable $packageName")
    }

    /**
     * List all disable packages
     * @return The list of disabled packages
     */
    fun listDisabledPackages(): List<String> {
        return su.exec("pm list packages -d | cut -d ':' -f 2").split("\n")
    }
}