package fr.hazegard.drfreeze.repository

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import fr.hazegard.drfreeze.FreezeDatabase
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg

class DbWrapper(private val context: Context) {
    private val driver: SqlDriver = AndroidSqliteDriver(FreezeDatabase.Schema, context, "query.db")

    private val database = FreezeDatabase(driver)

    private val query = database.packageAppQueries

    fun getAllPackages(): List<PackageApp> {
        return query.selectAll(mapper = packageMapper).executeAsList()
    }

    private val packageMapper: ((String, String, Boolean?, Boolean?) -> PackageApp) = { package_name: String, application_name: String, is_disabled: Boolean?, is_uninstalled: Boolean? ->
        PackageApp(
                Pkg(package_name),
                application_name,
                is_disabled ?: false,
                is_uninstalled ?: false)
    }

    fun getPackage(packageName: String): PackageApp {
        return query.selectOne(packageName, mapper = packageMapper).executeAsOne()
    }

    fun getAllEnabledPackages(): List<PackageApp> {
        return query.selectAllEnabledPackages(packageMapper).executeAsList()
    }

    fun getAllDisabledPackages(): List<PackageApp> {
        return query.selectAllDisabledPackages(packageMapper).executeAsList()
    }

    fun insertOne(packageApp: PackageApp) {
        with(packageApp) {
            query.insertOne(pkg.s, appName, isDisabled, isDisabled)
        }
    }

    fun getTrackedPackages(): List<PackageApp> {
        return query.selectTrackedPackages(packageMapper).executeAsList()
    }

    fun totoA() {
        driver.newTransaction().transaction {
        }
    }
}