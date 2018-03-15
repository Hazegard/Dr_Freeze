package fr.hazegard.freezator

import android.content.Context

/**
 * Created by maxime on 05/03/18.
 */
class SharedPreferenceHelper() {
    private val WATCHED_APPLICATION = "WATCHED_APPLICATION"
    fun saveWatchedApplication(context: Context, mapWatchedApplication: Map<String, Boolean>) {
        val prefs = context.getSharedPreferences(WATCHED_APPLICATION, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        mapWatchedApplication.filter {
            it.value
        }.map {
            edit.putBoolean(it.key, it.value)
        }
        edit.apply()
    }

    fun getWatchedApplication(context: Context): MutableMap<String, Boolean> {
        val map = context.getSharedPreferences(WATCHED_APPLICATION, Context.MODE_PRIVATE).all
        val mapWatchedApplication: MutableMap<String, Boolean> = HashMap()
        map.map {
            if (it.value is Boolean) {
                mapWatchedApplication[it.key] = it.value as Boolean
            }
        }
        return mapWatchedApplication
    }
}