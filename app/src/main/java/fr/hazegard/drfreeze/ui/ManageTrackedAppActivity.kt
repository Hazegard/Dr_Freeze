package fr.hazegard.drfreeze.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
            packageUtils.enablePackage(trackedPackageAdapter.managedPackage[position].pkg)
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

    @Inject
    lateinit var batchUpdate: BatchUpdate
    private lateinit var menu: Menu

    private val stopBatchUpdateReceiver: StopBatchUpdateReceiver = StopBatchUpdateReceiver()

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
            trackedPackageAdapter = trackedPackageAdapterFactory.getTrackedPackageAdapter(
                    this@ManageTrackedAppActivity,
                    this@ManageTrackedAppActivity,
                    listTrackedApp)
            val rows = computeSpan()
            val layout = GridLayoutManager(this@ManageTrackedAppActivity, rows).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (trackedPackageAdapter.getItemViewType(position)) {
                            TrackedPackageAdapter.HEADER -> rows
                            TrackedPackageAdapter.ITEM -> 1
                            else -> 1
                        }
                    }
                }
            }

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
        this.menu = menu
        if (batchUpdate.isUpdateModeEnabled()) {
            showUpdateModeEnabled()
        } else {
            showUpdateModeDisabled()
        }
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
            R.id.menu_enable_update_mode -> {
                enableUpdateMode()
                true
            }
            R.id.menu_disable_update_mode -> {
                disableUpdateMode()
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

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(ACTION_STOP_BATCH_UPDATE)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(stopBatchUpdateReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stopBatchUpdateReceiver)
    }

    /**
     * Get a list of tracked packages, sorted by application name
     * @return THe list of tracked packages
     */
    private fun getTrackedPackagesAsync(): Deferred<List<PackageApp>> {
        return GlobalScope.async { appsManager.getTrackedPackages() }
    }

    private fun enableUpdateMode() {
        showUpdateModeEnabled()
        batchUpdate.enableUpdateMode()
        trackedPackageAdapter.updateHeader()
    }

    private fun showUpdateModeEnabled() {
        this.menu.findItem(R.id.menu_enable_update_mode).isVisible = false
        this.menu.findItem(R.id.menu_disable_update_mode).isVisible = true

    }

    private fun showUpdateModeDisabled() {
        this.menu.findItem(R.id.menu_enable_update_mode).isVisible = true
        this.menu.findItem(R.id.menu_disable_update_mode).isVisible = false
    }

    private fun disableUpdateMode() {
        showUpdateModeDisabled()
        batchUpdate.disableUpdateMode()
        trackedPackageAdapter.updateHeader()
    }

    companion object {
        const val ACTION_STOP_BATCH_UPDATE = "fr.hazegard.dr_freeze.ACTION_STOP_BATCH_UPDATE"
        fun newIntent(context: Context): Intent {
            return Intent(context, ManageTrackedAppActivity::class.java)
        }
    }

    inner class StopBatchUpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_STOP_BATCH_UPDATE) {
                disableUpdateMode()
            }
        }
    }
}
