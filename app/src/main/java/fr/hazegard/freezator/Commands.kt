package fr.hazegard.freezator

class Commands {
    private val su = Su.instance
    fun enablePackage(packageName: String): String {
        return su.exec("pm enable $packageName")
    }

    fun disablePackage(packageName: String): String {
        return su.exec("pm disable $packageName")
    }

    fun listDisabledPackages(): List<String> {
        return su.exec("pm list packages -d | cut -d ':' -f 2").split("\n")
    }
}