package fr.hazegard.drfreeze.repository

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import fr.hazegard.drfreeze.FreezeDatabase
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import javax.inject.Inject

class DbWrapper @Inject constructor(private val context: Context) {
    private val driver: SqlDriver = AndroidSqliteDriver(FreezeDatabase.Schema, context, "query.db")

    private val database = FreezeDatabase(driver)

    private val query = database.packageAppQueries

    /**
     * Get all tracked packages
     * @return The list of all tracked packages
     */
    fun getAllPackages(): List<PackageApp> {
        return query.selectAll(mapper = packageMapper).executeAsList()
    }

    private val packageMapper: ((id: Long, String, String, Boolean) -> PackageApp) = { _, package_name: String, application_name: String, doNotify: Boolean ->
        PackageApp(Pkg(package_name), application_name, doNotify)
    }


    fun getPackage(pkg: Pkg): PackageApp {
        return query.selectOne(pkg.s.hashCode().toLong(), mapper = packageMapper).executeAsOne()
    }

    /**
     * Insert or Update
     */
    fun insertOrUpdateOne(packageApp: PackageApp) {
        with(packageApp) {
            query.insertOne(packageApp.id(), pkg.s, appName, doNotify)
        }
    }

    fun deletePackage(packageApp: PackageApp) {
        query.deleteOne(packageApp.id())
    }

    fun updateNotificationStatus(packageApp: PackageApp) {
        query.updateNotification(packageApp.doNotify, packageApp.id())
    }

    fun getNotificationStatus(pkg: Pkg): Boolean {
        return query.doNotify(pkg.s.hashCode().toLong()).executeAsOne()
    }
}