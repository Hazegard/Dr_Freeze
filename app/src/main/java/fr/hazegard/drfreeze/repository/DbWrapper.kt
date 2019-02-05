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

    fun getAllPackages(): List<PackageApp> {
        return query.selectAll(mapper = packageMapper).executeAsList()
    }

    private val packageMapper: ((id: Long, String, String) -> PackageApp) = { _, package_name: String, application_name: String ->
        PackageApp(Pkg(package_name), application_name)
    }

    fun getPackage(pkg: Pkg): PackageApp {
        return query.selectOne(pkg.s.hashCode().toLong(), mapper = packageMapper).executeAsOne()
    }

    fun insertOrUpdateOne(packageApp: PackageApp) {
        with(packageApp) {
            query.insertOne(packageApp.id(), pkg.s, appName)
        }
    }

    fun deletepackage(packageApp: PackageApp) {
        query.deleteOne(packageApp.id())
    }
}