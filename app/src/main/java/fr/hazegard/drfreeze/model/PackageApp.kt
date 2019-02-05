package fr.hazegard.drfreeze.model

data class PackageApp(val pkg: Pkg,
                      val appName: String,
                      var isInstalled: Boolean = false,
                      var isEnabled: Boolean = false)