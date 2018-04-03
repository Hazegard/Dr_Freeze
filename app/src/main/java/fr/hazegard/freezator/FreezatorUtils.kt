package fr.hazegard.freezator

import android.content.Context

/**
 * Created by hazegard on 16/03/18.
 */
class FreezatorUtils {
    companion object {
        fun getEnabledAndMonitored(context: Context): List<String> {
            val sp = SharedPreferenceHelper(context)
            val watchedApplications: List<String> = sp.getListMonitoredApplication()

            val disabledApps: List<String> = AppsManager(context).listDisabledApp()

            return watchedApplications.minus(disabledApps).toList()
        }
    }
}