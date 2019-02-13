package fr.hazegard.drfreeze.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.PreferencesHelper
import fr.hazegard.drfreeze.R
import javax.inject.Inject


class SettingsActivity : AppCompatActivity(), SettingsFragment.OnSettingChangeListener {
    override fun onFilterLauncherAppChanged() {
        newFilterLauncherApp = preferencesHelper.isOnlyLauncherApp()
    }

    override fun onNotificationChanged() {
        newNotificationSettings = preferencesHelper.isNotificationDisabled()
    }

    override fun onFilterSystemAppChanged() {
        newFilterSystemApp = preferencesHelper.isSystemAppsEnabled()
    }

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    private var prevNotificationSettings = false
    private var prevFilterLauncherApp = false
    private var prevFilterSystemApp = false

    private var newNotificationSettings = false
    private var newFilterLauncherApp = false
    private var newFilterSystemApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        FreezeApplication.appComponent.inject(this)
        prevNotificationSettings = preferencesHelper.isNotificationDisabled()
        prevFilterLauncherApp = preferencesHelper.isOnlyLauncherApp()
        prevFilterSystemApp = preferencesHelper.isSystemAppsEnabled()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val fragment = SettingsFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
    }

    override fun onBackPressed() {
        val result = Intent()
        val hasFiltersChanged = (newFilterSystemApp != prevFilterSystemApp) || (newFilterLauncherApp != prevFilterLauncherApp)
        val hasNotificationsChanged = newNotificationSettings != prevNotificationSettings
        result.putExtra(UPDATE_FILTER, hasFiltersChanged)
        result.putExtra(UPDATE_NOTIFICATIONS, hasNotificationsChanged)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
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