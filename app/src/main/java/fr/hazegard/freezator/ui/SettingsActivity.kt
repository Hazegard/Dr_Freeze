package fr.hazegard.freezator.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import fr.hazegard.freezator.R


class SettingsActivity : AppCompatActivity(), SettingsFragment.OnListAppsSettingChangeListener {
    private var hasChanged = false
    override fun onListAppsSettingChange() {
        hasChanged = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        result.putExtra(RESULT, hasChanged)
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
        const val RESULT = "REQUEST_UPDATE_APP_LIST"
        const val REQUEST_UPDATE_APP_LIST_CODE = 42

        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }


}