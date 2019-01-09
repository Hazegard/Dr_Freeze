package fr.hazegard.freezator.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import fr.hazegard.freezator.PackageUtils
import fr.hazegard.freezator.R
import kotlinx.android.synthetic.main.row_manage_apps.view.*

class TrackedAppAdatper(context: Context, private var managedApp: List<String>) : RecyclerView.Adapter<TrackedAppAdatper.ManagedAppHolder>() {
    override fun onBindViewHolder(holder: ManagedAppHolder, position: Int) {
        val appName = managedApp[position]
        holder.setContent(appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagedAppHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_manage_apps, parent, false)
        return ManagedAppHolder(itemView, parent.context)
    }

    override fun getItemCount(): Int {
        return managedApp.size
    }


    inner class ManagedAppHolder(private val view: View, private val context: Context) : RecyclerView.ViewHolder(view) {

        fun setContent(appName: String) = with(view) {

            val name = PackageUtils.getPackageName(context, appName)
            manage_app_name.text = name
            manage_add_shortcut.setOnClickListener {
                Log.d("Manage", "add_shortcut - $appName")
            }
            manage_add_shortcut.setOnLongClickListener {
                Toast.makeText(context, "Add Shortcut for $name", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }
            manage_freeze_app.setOnClickListener {
                Log.d("Manage", "freeze - $appName")
            }
            manage_freeze_app.setOnLongClickListener {
                Toast.makeText(context, "Freeze $name", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true

            }
            manage_untrack_app.setOnClickListener {
                Log.d("Manage", "Untrack - $appName")
            }
            manage_untrack_app.setOnLongClickListener {
                Toast.makeText(context, "Stop tracking for$name", Toast.LENGTH_SHORT).show()
                return@setOnLongClickListener true
            }
            manage_app_icon.setImageDrawable(PackageUtils.getPackageIconDrawable(context, appName))

        }

    }
}