package fr.hazegard.drfreeze.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.PackageManager
import fr.hazegard.drfreeze.PackageUtils
import fr.hazegard.drfreeze.R
import fr.hazegard.drfreeze.extensions.onAnimationEnd
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import kotlinx.android.synthetic.main.activity_list_packages.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates


class ListPackagesActivity : AppCompatActivity() {
    @Inject
    lateinit var packageUtils: PackageUtils

    @Inject
    lateinit var packageAdapterFactory: PackageAdapter.Companion.Factory
    private lateinit var packageAdapter: PackageAdapter
    private var menu: Menu? = null
    private var sendDoUpdate = false
    private var listPackage: List<PackageApp> by Delegates.observable(
            Collections.emptyList()) { _, _, newValue ->
        runOnUiThread {
            main_view_animator.displayedChild = if (newValue.isEmpty()) {
                1
            } else {
                2
            }
        }
    }
    private var isEdit: Boolean by Delegates.observable(false) { _, _, newValue ->
        packageAdapter.isEdit = newValue
        if (newValue) {
            package_fab.hide()
        } else {
            package_fab.show()
        }
        menu?.findItem(R.id.packageList_cancel)?.isVisible = newValue
        menu?.findItem(R.id.packageList_validate)?.isVisible = newValue
    }

    @Inject
    lateinit var packageManager: PackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        FreezeApplication.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_packages)
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
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        initListView(savedInstanceState)
        package_fab.setOnClickListener {
            isEdit = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.packageList_validate -> {
                validateChanges()
            }
            R.id.packageList_cancel -> {
                resetAdapterContent()
            }
            R.id.menu_settings -> {
                startActivityForResult(SettingsActivity.newIntent(this@ListPackagesActivity),
                        SettingsActivity.REQUEST_UPDATE_APP_LIST_CODE)
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        isEdit = false
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SettingsActivity.REQUEST_UPDATE_APP_LIST_CODE && resultCode == Activity.RESULT_OK
                && data?.getBooleanExtra(SettingsActivity.UPDATE_FILTER, false) == true) {
            GlobalScope.launch {
                listPackage = getPackagesAsync().await()
                runOnUiThread { packageAdapter.updateList(listPackage) }
            }
        }
    }

    /**
     * Reset the checkbox to the previous saved state
     */
    private fun resetAdapterContent() {
        val trackedPackages: MutableMap<Pkg, PackageApp> = packageManager.getTrackedPackagesAsMap().toMutableMap()
        packageAdapter.trackedPackages = trackedPackages
    }

    /**
     * Save the current checkbox state
     */
    private fun validateChanges() {
        GlobalScope.launch {
            with(packageAdapter) {
                packageManager.updateTrackedPackages(packagesToAdd.values.toList(), packagesToRemove.values.toList())
            }

        }
    }

    private fun getPackagesAsync(): Deferred<List<PackageApp>> {
        return GlobalScope.async {
            packageManager.getPackages()
        }
    }

    /**
     * Initialize the listView, restore the state if it exists
     * @param savedInstanceState the saved state to restore
     */
    private fun initListView(savedInstanceState: Bundle?) {
        GlobalScope.launch {
            listPackage = getPackagesAsync().await()
            val trackedPackages: MutableMap<Pkg, PackageApp> = packageManager.getTrackedPackagesAsMap().toMutableMap()
            val layout: RecyclerView.LayoutManager = LinearLayoutManager(
                    this@ListPackagesActivity, RecyclerView.VERTICAL, false)
            var doEdit = false
            savedInstanceState?.let {
                doEdit = it.getBoolean(STATE_IS_EDIT)
                val mapSavedAppToAdd: MutableMap<Pkg, PackageApp>? = it.getStringArray(STATE_APP_TO_ADD)?.fold(mutableMapOf()) { acc, curr ->
                    acc[Pkg(curr)] = packageUtils.safeCreatePackageApp(Pkg(curr))
                    return@fold acc
                }
                val mapSavedAppToRemove: MutableMap<Pkg, PackageApp>? = it.getStringArray(STATE_APP_TO_REMOVE)?.fold(mutableMapOf()) { acc, curr ->
                    acc[Pkg(curr)] = packageUtils.safeCreatePackageApp(Pkg(curr))
                    return@fold acc
                }
                mapSavedAppToAdd?.let { packages ->
                    packageAdapterFactory.addPackagesToAdd(packages)
                    trackedPackages.putAll(packages)
                }
                mapSavedAppToRemove?.let { packages ->
                    packageAdapterFactory.addPackagesToRemove(packages)
                    trackedPackages.keys.removeAll(packages.keys)
                }
            }

            packageAdapter = packageAdapterFactory.get(listPackage, trackedPackages) {
                sendDoUpdate = true
            }
            isEdit = doEdit

            runOnUiThread {
                with(packageList) {
                    layoutManager = layout
                    adapter = packageAdapter
                    visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.package_list_menu, menu)
        menuInflater.inflate(R.menu.main, menu)

        menu.findItem(R.id.packageList_cancel)?.isVisible = isEdit
        menu.findItem(R.id.packageList_validate)?.isVisible = isEdit
        return true
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(STATE_IS_EDIT, isEdit)
        if (isEdit) {
            if (packageAdapter.packagesToAdd.isNotEmpty()) {
                outState?.putStringArray(STATE_APP_TO_ADD, packageAdapter.packagesToAdd.keys.map { it.s }.toTypedArray())
            }
            if (packageAdapter.packagesToRemove.isNotEmpty()) {
                outState?.putStringArray(STATE_APP_TO_REMOVE, packageAdapter.packagesToRemove.keys.map { it.s }.toTypedArray())
            }
        }
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        val result = Intent()
        result.putExtra(RESULT, sendDoUpdate)
        setResult(Activity.RESULT_OK, result)
        super.onBackPressed()
    }

    companion object {
        private const val TAG: String = "ListPackagesActivity"
        const val UPDATE_TRACKED_APPS_CODE = 64
        const val RESULT = "UPDATE_TRACKED_APPS"
        private const val STATE_APP_TO_REMOVE = "STATE_APP_TO_REMOVE"
        private const val STATE_IS_EDIT = "STATE_IS_EDIT "
        private const val STATE_APP_TO_ADD = "STATE_APP_TO_ADD"
        fun newIntent(context: Context): Intent {
            return Intent(context, ListPackagesActivity::class.java)
        }
    }
}