package fr.hazegard.freezator

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.Ref
import org.jetbrains.anko.coroutines.experimental.asReference


class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private lateinit var processAdapter: ProcessAdapter
    private lateinit var menu: Menu
    private val appsManager by lazy {
        AppsManager(this@MainActivity)
    }
    private val notificationUtils: NotificationUtils by lazy {
        NotificationUtils(this@MainActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListView()
        button_dis.setOnClickListener {
            asyncDisablePackage("org.mozilla.klar")
        }
        button_ena.setOnClickListener {
            asyncEnablePackage("org.mozilla.klar")
            notificationUtils.notify("org.mozilla.klar")
        }

        button_start.setOnClickListener {
            asyncStart("org.mozilla.klar")
        }
        button_onboot.setOnClickListener {
            async(CommonPool) {
                val disPackages = appsManager.listDisabledPackages()
                disPackages.forEach {
                    notificationUtils.notify(it.processName)
                }
            }
        }
        process_fab.setOnClickListener {
            processAdapter.isEdit = true
            process_fab.hide()
            menu.findItem(R.id.processList_cancel).isVisible = true
            menu.findItem(R.id.processList_validate).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.processList_validate -> {
                processAdapter.validateChange()
            }
            R.id.processList_cancel -> {
                processAdapter.cancelChange()
            }
        }
        processAdapter.isEdit = false
        process_fab.show()
        menu.findItem(R.id.processList_cancel).isVisible = false
        menu.findItem(R.id.processList_validate).isVisible = false
        return super.onOptionsItemSelected(item)
    }

    private fun initListView() {
        val listPackage: List<ApplicationInfo> = appsManager.installedPackages
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
                this@MainActivity, LinearLayoutManager.VERTICAL, false)
        processAdapter = ProcessAdapter(this@MainActivity, listPackage)
        processList.layoutManager = layoutManager
        processList.adapter = processAdapter
        processList.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.processlistmenu, menu)
        return true
    }

    fun test() {
        Log.d("cor", "start")
        val ref: Ref<MainActivity> = this.asReference()
        val disPack = async(CommonPool) {
            Log.d("CommonPool", "start")
            val disabledPackages = appsManager.listDisabledPackages()

            Log.d("CommonPool", "toastpost")
            return@async disabledPackages
        }
        async(UI) {
            Log.d("UI", "start")
            Toast.makeText(ref().baseContext, disPack.await().size.toString(), Toast.LENGTH_SHORT).show()
            Log.d("UI", "stop")
        }
    }

    fun asyncDisablePackage(packageName: String): Deferred<String> {
        return async(CommonPool) {
            return@async appsManager.disablePackage(packageName)
        }
    }

    fun asyncEnablePackage(packageName: String): Deferred<String> {
        return async(CommonPool) {
            return@async appsManager.enablePackage(packageName)

        }
    }


    fun asyncStart(packageName: String) {
        async(CommonPool) {
            val test = asyncEnablePackage(packageName)
            Log.d("async", test.await())
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                Log.d("async", "intent good")
                startActivity(launchIntent)//null pointer check in case package name was not found
            } else {
                Log.d("async", "intent null")
            }
        }
    }


}
