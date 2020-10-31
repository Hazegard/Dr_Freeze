package fr.hazegard.drfreeze.repository

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import fr.hazegard.drfreeze.db.FreezeDatabase
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import javax.inject.Inject

class DbWrapper @Inject constructor(context: Context) {
    private val driver: SqlDriver = AndroidSqliteDriver(
            FreezeDatabase.Schema,
            context,
            "query.db"
    )

    private val database = FreezeDatabase(driver)

    private val query = database.packageAppQueries

    /**
     * Get all tracked packages
     * @return The list of all tracked packages
     */
    fun getAllPackages(): List<PackageApp> {
        return query.selectAll(mapper = packageMapper).executeAsList()
    }

    private val packageMapper: ((id: Long, String, String, Boolean, Boolean) -> PackageApp) = { _, package_name: String, application_name: String, doNotify: Boolean, _ ->
        PackageApp(Pkg(package_name), application_name, doNotify)
    }


    /**
     * Get the package from the database
     */
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

    /**
     * Delete the package from the database
     */
    fun deletePackage(packageApp: PackageApp) {
        query.deleteOne(packageApp.id())
    }

    /**
     * Update the field 'doNotify' in the database
     * @param packageApp The package with the updated notification status
     */
    fun updateNotificationStatus(packageApp: PackageApp) {
        query.updateNotification(packageApp.doNotify, packageApp.id())
    }

    /**
     * Get the notification status
     * @return Whether the application must be notified
     */
    fun getNotificationStatus(pkg: Pkg): Boolean {
        return query.doNotify(pkg.s.hashCode().toLong()).executeAsOne()
    }

    /**
     * Get all packages with enabled notifications
     */
    fun selectPackagesToNotify(): List<PackageApp> {
        return query.selectAllWithNotificationEnabled(mapper = packageMapper).executeAsList()
    }

    /**
     * Get all packages that were enabled with the batch update mode
     */
    fun selectFlaggedUpdatePackages(): List<PackageApp> {
        return query.selectFlaggedUpdate(mapper = packageMapper).executeAsList()
    }

    /**
     * Set the update flag to true
     */
    fun setFlagUpdate(packageApp: PackageApp) {
        query.updateFlagUpdateStatus(true, packageApp.id())
    }

    /**
     * Set the update flag to false
     */
    fun resetFlagUpdate(packageApp: PackageApp) {
        query.updateFlagUpdateStatus(false, packageApp.id())
    }

}
