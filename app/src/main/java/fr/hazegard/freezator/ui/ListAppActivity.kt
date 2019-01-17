package fr.hazegard.freezator.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import fr.hazegard.freezator.PackageManager
import fr.hazegard.freezator.R
import fr.hazegard.freezator.extensions.onAnimationEnd
import fr.hazegard.freezator.model.PackageApp
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*


class ListAppActivity : AppCompatActivity() {
    private val TAG: String = "ListAppActivity"
    private lateinit var packageAdapter: PackageAdapter
    private lateinit var menu: Menu
    private var sendDoUpdate = false
    private var listPackage: List<PackageApp> = Collections.emptyList()
        set(value) {
            runOnUiThread {
                main_view_annimator.displayedChild = if (value.isEmpty()) {
                    1
                } else {
                    2
                }
            }
            field = value
        }
    private val packageManager by lazy {
        PackageManager(this@ListAppActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        initListView()
        package_fab.setOnClickListener {
            packageAdapter.isEdit = true
            package_fab.hide()
            menu.findItem(R.id.packageList_cancel).isVisible = true
            menu.findItem(R.id.packageList_validate).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.packageList_validate -> {
                packageAdapter.validateChange()
            }
            R.id.packageList_cancel -> {
                packageAdapter.cancelChange()
            }
            R.id.menu_settings -> {
                startActivityForResult(SettingsActivity.newIntent(this@ListAppActivity),
                        SettingsActivity.REQUEST_UPDATE_APP_LIST_CODE)
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        packageAdapter.isEdit = false
        package_fab.show()
        menu.findItem(R.id.packageList_cancel).isVisible = false
        menu.findItem(R.id.packageList_validate).isVisible = false
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SettingsActivity.REQUEST_UPDATE_APP_LIST_CODE && resultCode == Activity.RESULT_OK) {
            if (data?.getBooleanExtra(SettingsActivity.RESULT, false) == true) {
                GlobalScope.launch {
                    listPackage = getPackages().await()
                    runOnUiThread { packageAdapter.updateList(listPackage) }
                }
            }
        }
    }

    private fun getPackages(): Deferred<List<PackageApp>> {
        return GlobalScope.async {
            packageManager.getPackages()
        }
    }

    private fun initListView() {
        GlobalScope.launch {
            listPackage = getPackages().await()
            val layout: RecyclerView.LayoutManager = LinearLayoutManager(
                    this@ListAppActivity, LinearLayoutManager.VERTICAL, false)
            packageAdapter = PackageAdapter(this@ListAppActivity, listPackage) {
                sendDoUpdate = true
            }
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
        return true
    }

    override fun onBackPressed() {
        val result = Intent()
        result.putExtra(RESULT, sendDoUpdate)
        setResult(Activity.RESULT_OK, result)
        super.onBackPressed()
    }

    companion object {
        const val UPDATE_TRACKED_APPS_CODE = 64
        const val RESULT = "UPDATE_TRACKED_APPS"
        fun newIntent(context: Context): Intent {
            return Intent(context, ListAppActivity::class.java)
        }
    }
}