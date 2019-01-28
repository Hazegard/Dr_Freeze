package fr.hazegard.drfreeze.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.NotificationUtils
import fr.hazegard.drfreeze.PackageManager
import fr.hazegard.drfreeze.R
import fr.hazegard.drfreeze.extensions.onAnimationEnd
import fr.hazegard.drfreeze.model.PackageApp
import kotlinx.android.synthetic.main.activity_manage_tracked_app.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates

class ManageTrackedAppActivity : AppCompatActivity() {
    private lateinit var trackedPackageAdapter: TrackedPackageAdapter
    /**
     * Updating the list of tracked packages update also the view depending on the list size
     */
    private var listTrackedApp: List<PackageApp> by Delegates.observable(
            Collections.emptyList()) { _, _, newValue ->
        runOnUiThread {
            tracked_view_animator.displayedChild = if (newValue.isEmpty()) {
                1
            } else {
                2
            }
        }
    }

    @Inject
    lateinit var appsManager: PackageManager

    @Inject
    lateinit var notificationUtils: NotificationUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_tracked_app)
        FreezeApplication.appComponent.inject(this)
        with(animation_android.drawable) {
            (this as Animatable).start()
            onAnimationEnd {
                if (isVisible) {
                    runOnUiThread {
                        start()
                    }
                }
            }
        }
        initListView()
        setSupportActionBar(findViewById(R.id.toolbar))
        no_tracked_applications.setOnClickListener {
            val listAppActivityIntent = ListPackagesActivity.newIntent(this)
            startActivityForResult(listAppActivityIntent, ListPackagesActivity.UPDATE_TRACKED_APPS_CODE)
        }
    }

    /**
     * Initialize the list view
     */
    private fun initListView() {
        GlobalScope.launch {
            listTrackedApp = getTrackedPackagesAsync().await()
            val layout: RecyclerView.LayoutManager = GridLayoutManager(this@ManageTrackedAppActivity, 2)
            trackedPackageAdapter = TrackedPackageAdapter(this@ManageTrackedAppActivity,
                    appsManager,
                    notificationUtils,
                    listTrackedApp,
                    {
                        finishAffinity()
                        System.exit(0)
                    },
                    {
                        listTrackedApp = appsManager.getTrackedPackages()
                        runOnUiThread {
                            trackedPackageAdapter.updateList(listTrackedApp)
                        }
                    })

            runOnUiThread {
                with(managed_app_list)
                {
                    layoutManager = layout
                    adapter = trackedPackageAdapter
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tracked_app_menu, menu)
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_settings -> {
                val settingActivityIntent = SettingsActivity.newIntent(this)
                startActivityForResult(settingActivityIntent, SettingsActivity.REQUEST_UPDATE_APP_LIST_CODE)
                true
            }
            R.id.menu_list_apps -> {
                val listAppActivityIntent = ListPackagesActivity.newIntent(this)
                startActivityForResult(listAppActivityIntent, ListPackagesActivity.UPDATE_TRACKED_APPS_CODE)
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ListPackagesActivity.UPDATE_TRACKED_APPS_CODE && resultCode == Activity.RESULT_OK) {
            GlobalScope.launch {
                listTrackedApp = getTrackedPackagesAsync().await()
                runOnUiThread { trackedPackageAdapter.updateList(listTrackedApp) }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Get a list of tracked packages, sorted by application name
     * @return THe list of tracked packages
     */
    private fun getTrackedPackagesAsync(): Deferred<List<PackageApp>> {
        return GlobalScope.async { appsManager.getTrackedPackages() }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ManageTrackedAppActivity::class.java)
        }
    }
}
