package fr.hazegard.freezator.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import fr.hazegard.freezator.PackageManager
import fr.hazegard.freezator.R
import fr.hazegard.freezator.extensions.onAnimationEnd
import fr.hazegard.freezator.model.PackageApp
import kotlinx.android.synthetic.main.activity_manage_tracked_app.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class ManageTrackedAppActivity : AppCompatActivity() {
    private lateinit var trackedPackageAdapter: TrackedPackageAdapter
    /**
     * Updating the list of tracked packages update also the view depending on the list size
     */
    private var listTrackedApp: List<PackageApp> = Collections.emptyList()
        set(value) {
            runOnUiThread {
                tracked_view_animator.displayedChild = if (value.isEmpty()) {
                    1
                } else {
                    2
                }
            }
            field = value
        }
    private val appsManager by lazy {
        PackageManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val listAppActivityIntent = ListAppActivity.newIntent(this)
            startActivityForResult(listAppActivityIntent, ListAppActivity.UPDATE_TRACKED_APPS_CODE)
        }
    }

    /**
     * Initialize the list view
     */
    private fun initListView() {
        GlobalScope.launch {
            listTrackedApp = getTrackedPackages().await()
            val layout: RecyclerView.LayoutManager = GridLayoutManager(this@ManageTrackedAppActivity, 2)
            trackedPackageAdapter = TrackedPackageAdapter(this@ManageTrackedAppActivity, listTrackedApp,
                    {
                        finishAffinity()
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
                val listAppActivityIntent = ListAppActivity.newIntent(this)
                startActivityForResult(listAppActivityIntent, ListAppActivity.UPDATE_TRACKED_APPS_CODE)
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
        if (requestCode == ListAppActivity.UPDATE_TRACKED_APPS_CODE && resultCode == Activity.RESULT_OK) {
            GlobalScope.launch {
                listTrackedApp = getTrackedPackages().await()
                runOnUiThread { trackedPackageAdapter.updateList(listTrackedApp) }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Get a list of tracked packages, sorted by application name
     * @return THe list of tracked packages
     */
    private fun getTrackedPackages(): Deferred<List<PackageApp>> {
        return GlobalScope.async { appsManager.getTrackedPackages() }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, ManageTrackedAppActivity::class.java)
            return intent
        }
    }
}
