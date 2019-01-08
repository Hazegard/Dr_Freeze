package fr.hazegard.freezator.ui

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
import fr.hazegard.freezator.AppsManager
import fr.hazegard.freezator.NotificationUtils
import fr.hazegard.freezator.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.jetbrains.anko.coroutines.experimental.Ref
import org.jetbrains.anko.coroutines.experimental.asReference
import android.support.v4.view.accessibility.AccessibilityEventCompat.setAction
import fr.hazegard.freezator.R.mipmap.ic_launcher
import android.content.Intent
import android.os.Build
import android.support.v4.content.pm.ShortcutManagerCompat.requestPinShortcut
import android.R.attr.label
import android.R.attr.shortcutId
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.support.v4.content.pm.ShortcutManagerCompat
import fr.hazegard.freezator.PackageUtils


class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private lateinit var processAdapter: ProcessAdapter
    private lateinit var menu: Menu
    private val appsManager by lazy {
        AppsManager(this@MainActivity)
    }
//    private val notificationUtils: NotificationUtils by lazy {
//        NotificationUtils(this@MainActivity)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListView()
        button_dis.setOnClickListener {
            Toast.makeText(this@MainActivity, "DIS", Toast.LENGTH_LONG).show()
            GlobalScope.async(Dispatchers.IO) {
                val res = appsManager.disablePackage("org.mozilla.focus")
                Log.d("dis", res)
                Toast.makeText(this@MainActivity, res, Toast.LENGTH_LONG).show()
            }
        }
        button_ena.setOnClickListener {
            GlobalScope.async(Dispatchers.IO) {
                val res = appsManager.enablePackage("org.mozilla.focus")
                Log.d("ena", res)
                NotificationUtils.notify(this@MainActivity, "org.mozilla.focus")
                Toast.makeText(this@MainActivity, res, Toast.LENGTH_LONG).show()
            }
        }

        button_start.setOnClickListener {
            asyncStart("org.mozilla.focus")
        }
        button_onboot.setOnClickListener {
            GlobalScope.async(Dispatchers.IO) {
                val disPackages = appsManager.listDisabledPackages()
                disPackages.forEach { it ->
                    NotificationUtils.notify(this@MainActivity, it.processName)
                }
            }
        }
        button_test.setOnClickListener {
            addShortcut("org.mozilla.focus")
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
        val disPack = GlobalScope.async(Dispatchers.Default) {
            Log.d("CommonPool", "start")
            val disabledPackages = appsManager.listDisabledPackages()

            Log.d("CommonPool", "toastpost")
            return@async disabledPackages
        }
        GlobalScope.async(Dispatchers.Main) {
            Log.d("UI", "start")
            Toast.makeText(ref().baseContext, disPack.await().size.toString(), Toast.LENGTH_SHORT).show()
            Log.d("UI", "stop")
        }
    }

    fun asyncDisablePackage(packageName: String): Deferred<String> {
        return GlobalScope.async(Dispatchers.Default) {
            return@async appsManager.disablePackage(packageName)
        }
    }

    fun asyncEnablePackage(packageName: String): Deferred<String> {
        return GlobalScope.async(Dispatchers.Default) {
            return@async appsManager.enablePackage(packageName)

        }
    }


    fun asyncStart(packageName: String) {
        Log.d("Start", "start")
        GlobalScope.async(Dispatchers.Default) {
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

    private fun addShortcut(packageName: String) {
        val label = PackageUtils.getPackageName(applicationContext, packageName)
        val icon = PackageUtils.getPackageIconBitmap(applicationContext, packageName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(applicationContext)) {
                val intent = ShortcutReceiverActivity.newIntent(applicationContext,packageName)
                val shortcutManager = getSystemService(ShortcutManager::class.java)
                val pinShortcutInfo = ShortcutInfo.Builder(applicationContext, "ID")
                        .setIcon(Icon.createWithBitmap(icon))
                        .setShortLabel(label)
                        .setIntent(intent)
                        .build()
                shortcutManager.requestPinShortcut(pinShortcutInfo, null)
            }
        } else {
            //Adding shortcut for MainActivity
            //on Home screen
            val shortcutIntent = Intent(applicationContext,
                    MainActivity::class.java)

            shortcutIntent.action = Intent.ACTION_MAIN

            val addIntent = Intent()
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "HelloWorldShortcut")
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(applicationContext,
                            R.drawable.snowflake))
            addIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
            addIntent.putExtra("duplicate", false)  //may it's already there so don't duplicate
            applicationContext.sendBroadcast(addIntent)
            Log.d("shortcut", "done")
        }
    }
}
