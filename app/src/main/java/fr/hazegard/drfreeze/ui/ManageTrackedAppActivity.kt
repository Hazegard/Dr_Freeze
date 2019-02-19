package fr.hazegard.drfreeze.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import fr.hazegard.drfreeze.*
import fr.hazegard.drfreeze.extensions.onAnimationEnd
import fr.hazegard.drfreeze.model.PackageApp
import kotlinx.android.synthetic.main.activity_manage_tracked_app.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject


class ManageTrackedAppActivity : AppCompatActivity(), TrackedPackageAdapter.OnClick {
    override fun onUnfreezeClick(position: Int) {
        GlobalScope.launch {
            packageUtils.enablePackage(trackedPackageAdapter.managedPackage[position])
            runOnUiThread {
                trackedPackageAdapter.updateItem(position)
            }
        }
    }

    override fun onAddShortCutCLick(position: Int) {
        packageUtils.addShortcut(this, trackedPackageAdapter.managedPackage[position])
    }

    override fun onFreezeClick(position: Int) {
        GlobalScope.launch {
            packageUtils.disablePackage(trackedPackageAdapter.managedPackage[position].pkg)
            runOnUiThread {
                trackedPackageAdapter.updateItem(position)
            }
        }
    }

    override fun onUntrackClick(position: Int) {
        GlobalScope.launch {
            appsManager.removeTrackedPackage(trackedPackageAdapter.managedPackage[position])
            runOnUiThread {
                trackedPackageAdapter.removeAt(position)
                updateView()
            }
        }
    }

    override fun onNotificationSwitchClick(position: Int, newState: Boolean) {
        trackedPackageAdapter.managedPackage[position].doNotify = newState
        appsManager.updateNotification(trackedPackageAdapter.managedPackage[position])
    }

    override fun onClickStartApplication(position: Int) {
        GlobalScope.launch {
            packageUtils.start(trackedPackageAdapter.managedPackage[position], this@ManageTrackedAppActivity)
            this@ManageTrackedAppActivity.finishAffinity()
        }
    }

    @Inject
    lateinit var trackedPackageAdapterFactory: TrackedPackageAdapter.Companion.Factory
    private lateinit var trackedPackageAdapter: TrackedPackageAdapter

    @Inject
    lateinit var packageUtils: PackageUtils

    @Inject
    lateinit var appsManager: PackageManager

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FreezeApplication.appComponent.inject(this)
        setContentView(R.layout.activity_manage_tracked_app)
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
            val listTrackedApp = getTrackedPackagesAsync().await().toMutableList()
            val layout = GridLayoutManager(this@ManageTrackedAppActivity, computeSpan())
            trackedPackageAdapter = trackedPackageAdapterFactory.getTrackedPackageAdapter(
                    this@ManageTrackedAppActivity,
                    this@ManageTrackedAppActivity,
                    listTrackedApp)

            runOnUiThread {
                with(managed_app_list) {
                    layoutManager = layout
                    adapter = trackedPackageAdapter
                }
                updateView()
            }
        }
    }

    private fun computeSpan(): Int {
        val displayMetrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val itemWidthPx = 200 * this.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
        return screenWidth / itemWidthPx
    }

    /**
     * Update the view depending on the size of tracked application list
     */
    private fun updateView() {
        runOnUiThread {
            tracked_view_animator.displayedChild = if (trackedPackageAdapter.managedPackage.isEmpty()) {
                1
            } else {
                2
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
        if (resultCode == Activity.RESULT_OK
                && ((requestCode == SettingsActivity.REQUEST_UPDATE_APP_LIST_CODE && data?.getBooleanExtra(SettingsActivity.UPDATE_NOTIFICATIONS, false) == true)
                        || (requestCode == ListPackagesActivity.UPDATE_TRACKED_APPS_CODE && data?.getBooleanExtra(ListPackagesActivity.RESULT, false) == true)
                        )) {
            GlobalScope.launch {
                val listTrackedApp = getTrackedPackagesAsync().await().toMutableList()
                runOnUiThread {
                    trackedPackageAdapter.updateList(listTrackedApp)
                    updateView()
                }
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
