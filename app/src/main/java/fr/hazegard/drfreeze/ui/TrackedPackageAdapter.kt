package fr.hazegard.drfreeze.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import fr.hazegard.drfreeze.NotificationUtils
import fr.hazegard.drfreeze.PackageManager
import fr.hazegard.drfreeze.R
import fr.hazegard.drfreeze.model.PackageApp
import kotlinx.android.synthetic.main.row_manage_apps.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * The adapter used to display tracked packages
 */
class TrackedPackageAdapter(private val c: Context,
                            private val packageManager: PackageManager,
                            private val notificationUtils: NotificationUtils,
                            private var managedPackage: List<PackageApp>,
                            private val callback: () -> Unit,
                            private val requestUpdate: () -> Unit)
    : RecyclerView.Adapter<TrackedPackageAdapter.ManagedAppHolder>() {

    private var listDisabledPackages = packageManager.getDisabledPackages()

    override fun onBindViewHolder(holder: ManagedAppHolder, position: Int) {
        val appName = managedPackage[position]
        holder.setContent(appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagedAppHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_manage_apps, parent, false)
        return ManagedAppHolder(itemView, packageManager, notificationUtils)
    }

    override fun getItemCount(): Int {
        return managedPackage.size
    }

    /**
     * Update the packages of packages and update the ui
     * @param packages The new list of packages
     */
    fun updateList(packages: List<PackageApp>) {
        listDisabledPackages = packageManager.getDisabledPackages()
        managedPackage = packages
        notifyDataSetChanged()
    }

    inner class ManagedAppHolder(private val view: View,
                                 private val packageManager: PackageManager,
                                 private val notificationUtils: NotificationUtils)
        : RecyclerView.ViewHolder(view) {

        /**
         * Set the content of a item
         * @param packageApp The package to be displayed
         */
        fun setContent(packageApp: PackageApp) {
            val isPkgEnabled = packageApp.isEnable(c.packageManager)
            with(view) {
                manage_app_name.text = packageApp.appName

                with(manage_card_view) {
                    setBackgroundColor(ContextCompat.getColor(c, if (!isPkgEnabled) {
                        R.color.colorBackgroundBlueLight
                    } else {
                        R.color.background
                    }))
                }

                with(manage_add_shortcut) {
                    setOnClickListener {
                        packageManager.addShortcut(c, packageApp)
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, "Add Shortcut for ${packageApp.appName}", Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                }

                with(manage_freeze_app) {
                    setOnClickListener {
                        GlobalScope.launch {
                            packageManager.disablePackage(packageApp.pkg)
                            notificationUtils.removeNotification(packageApp)
                            requestUpdate()
                        }
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, "Freeze ${packageApp.appName}", Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                    isEnabled = isPkgEnabled
                }

                with(manage_untrack_app) {
                    setOnClickListener {
                        packageManager.removeTrackedPackage(packageApp)
                        requestUpdate()
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, "Stop tracking ${packageApp.appName}", Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                }

                manage_app_icon.setImageDrawable(packageApp.getIconDrawable(c.packageManager))
                with(manage_card_view) {
                    setOnClickListener {
                        packageManager.start(packageApp, c)
                        callback()
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, "Start ${packageApp.appName}", Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }
}