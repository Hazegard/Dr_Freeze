package fr.hazegard.freezator.ui

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import fr.hazegard.freezator.AppsManager
import fr.hazegard.freezator.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_manage_tracked_app.*

class ManageTrackedAppActivity : AppCompatActivity() {

    private lateinit var trackedAppAdapter: TrackedAppAdatper
    private val appsManager by lazy {
        AppsManager(this@ManageTrackedAppActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_tracked_app)
        initListView()
    }

    private fun initListView() {
        val listTrackedApp: List<String> = appsManager.getTrackedApp()
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(
                this@ManageTrackedAppActivity, 2)
        trackedAppAdapter = TrackedAppAdatper(this@ManageTrackedAppActivity, listTrackedApp)
        managed_app_list.layoutManager = layoutManager
        managed_app_list.adapter = trackedAppAdapter
    }

    companion object {
        fun newIntent(context: Context):Intent{
            val intent = Intent(context,ManageTrackedAppActivity::class.java)
            return intent
        }
    }
}
