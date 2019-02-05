package fr.hazegard.drfreeze.model

data class PackageApp(val pkg: Pkg,
                      val appName: String,
                      var doNotify: Boolean = true) {
    fun id() = pkg.s.hashCode().toLong()
}