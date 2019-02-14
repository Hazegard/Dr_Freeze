package fr.hazegard.drfreeze.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.PreferencesHelper
import fr.hazegard.drfreeze.R
import javax.inject.Inject


class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    private var prevNotificationSettings = false
    private var prevFilterLauncherApp = false
    private var prevFilterSystemApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        FreezeApplication.appComponent.inject(this)
        initPreviousSettings()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val fragment = SettingsFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
    }

    private fun initPreviousSettings() {
        prevNotificationSettings = preferencesHelper.isNotificationDisabled()
        prevFilterLauncherApp = preferencesHelper.isOnlyLauncherApp()
        prevFilterSystemApp = preferencesHelper.isSystemAppsEnabled()
    }

    override fun onBackPressed() {
        val result = Intent()
        val newFilterLauncherApp = preferencesHelper.isOnlyLauncherApp()
        val newNotificationSettings = preferencesHelper.isNotificationDisabled()
        val newFilterSystemApp = preferencesHelper.isSystemAppsEnabled()

        val hasFiltersChanged = (newFilterSystemApp != prevFilterSystemApp) || (newFilterLauncherApp != prevFilterLauncherApp)
        val hasNotificationsChanged = newNotificationSettings != prevNotificationSettings
        result.putExtra(UPDATE_FILTER, hasFiltersChanged)
        result.putExtra(UPDATE_NOTIFICATIONS, hasNotificationsChanged)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.about, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_about -> {
                LibsBuilder().withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withActivityTitle(getString(R.string.about))
                        .withLicenseShown(true)
                        .withExcludedLibraries("AndroidIconics", "fastadapter")
                        .withFields(R.string::class.java.fields)
                        .start(this)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        const val UPDATE_FILTER = "UPDATE_FILTER"
        const val UPDATE_NOTIFICATIONS = "UPDATE_NOTIFICATIONS"
        const val REQUEST_UPDATE_APP_LIST_CODE = 42

        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}