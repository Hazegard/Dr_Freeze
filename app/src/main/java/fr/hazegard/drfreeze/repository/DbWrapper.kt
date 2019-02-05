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

    private val packageMapper: ((id: Long, String, String) -> PackageApp) = { _, package_name: String, application_name: String ->
        PackageApp(Pkg(package_name), application_name)
    }

    fun getPackage(packageName: String): PackageApp {
        return query.selectOne(packageName, mapper = packageMapper).executeAsOne()
    }

    fun insertOrUpdateOne(packageApp: PackageApp) {
        with(packageApp) {
            query.insertOne(pkg.s.hashCode().toLong(), pkg.s, appName)
        }
    }
}