package fr.hazegard.freezator

import android.content.Context
import java.util.*

/**
 * Created by maxime on 05/03/18.
 */
class SharedPreferenceHelper(private val context: Context) {

    private fun saveTrackedApplication(listApp: List<String>) {
        val set = listApp.toSet()
        val prefs = context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putStringSet(LIST, set)
        edit.apply()
    }

    fun saveTrackedApplication(mapApp: MutableMap<String, Boolean>) {
        saveTrackedApplication(mapToList(mapApp))
    }

    fun getListTrackedApplications(): List<String> {
        return context.getSharedPreferences(TRACKED_APPLICATION, Context.MODE_PRIVATE)
                .getStringSet(LIST, HashSet<String>())?.toList() ?: Collections.emptyList()
    }

    fun getMapTrackedApplications(): MutableMap<String, Boolean> {
        return listToMap(getListTrackedApplications())
    }

    private fun listToMap(list: List<String>): MutableMap<String, Boolean> {
        return list.fold(HashMap()) { map, app ->
            map[app] = true
            return@fold map
        }
    }

    private fun mapToList(map: MutableMap<String, Boolean>): List<String> {
        return map.filter { it.value }.keys.toList()
    }

    companion object {
        private const val TRACKED_APPLICATION = "TRACKED_APPLICATION"
        private const val LIST = "LIST"
    }
}