package fr.hazegard.drfreeze.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import fr.hazegard.drfreeze.*
import fr.hazegard.drfreeze.model.PackageApp
import kotlinx.android.synthetic.main.row_manage_apps.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The adapter used to display tracked packages
 */
class TrackedPackageAdapter private constructor(
        private val packageManager: PackageManager,
        private val packageUtils: PackageUtils,
        private val notificationManager: NotificationManager,
        private val imageManager: ImageManager,
        private val c: Context,
        private var managedPackage: List<PackageApp>,
        private val onApplicationStarted: () -> Unit,
        private val onRequestUpdate: () -> Unit)
    : RecyclerView.Adapter<TrackedPackageAdapter.ManagedAppHolder>() {

    private var listDisabledPackages = packageManager.getDisabledPackages()

    override fun onBindViewHolder(holder: ManagedAppHolder, position: Int) {
        val appName = managedPackage[position]
        holder.setContent(appName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagedAppHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_manage_apps, parent, false)
        return ManagedAppHolder(itemView)
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

    inner class ManagedAppHolder(private val view: View)
        : RecyclerView.ViewHolder(view) {

        /**
         * Set the content of a item
         * @param packageApp The package to be displayed
         */
        fun setContent(packageApp: PackageApp) {
            val isPkgEnabled = packageUtils.isPackageEnabled(packageApp.pkg)
            val isPackageInstalled = packageUtils.isPackageInstalled(packageApp.pkg)
            with(view) {
                manage_app_name.text = packageApp.appName

                with(manage_card_view) {
                    setBackgroundColor(ContextCompat.getColor(c, if (!isPackageInstalled) {
                        R.color.backgroundDarker
                    } else if (!isPkgEnabled) {
                        R.color.colorBackgroundBlueLight
                    } else {
                        R.color.background
                    }))
                }

                with(manage_add_shortcut) {
                    setOnClickListener {
                        packageUtils.addShortcut(c, packageApp)
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, c.getString(R.string.button_add_shortcut, packageApp.appName), Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                }

                manage_freeze_app.setOnClickListener { }
                with(manage_freeze_app) {
                    setOnClickListener {
                        GlobalScope.launch {
                            packageUtils.disablePackage(packageApp.pkg)
                            notificationManager.removeNotification(packageApp)
                            onRequestUpdate.invoke()
                        }
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, context.getString(R.string.button_freeze_app, packageApp.appName), Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                    isEnabled = isPkgEnabled && isPackageInstalled
                }

                with(manage_untrack_app) {
                    setOnClickListener {
                        packageManager.removeTrackedPackage(packageApp)
                        onRequestUpdate.invoke()
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, context.getString(R.string.button_stop_tracking, packageApp.appName), Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                }

                manage_app_icon.setImageDrawable(imageManager.getCachedImage(packageApp))
                with(manage_card_view) {
                    setOnClickListener {
                        if (isPackageInstalled) {
                            packageUtils.start(packageApp, c)
                            onApplicationStarted.invoke()
                        } else {
                            Toast.makeText(context, context.getString(R.string.application_not_found), Toast.LENGTH_LONG).show()
                        }
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, context.getString(R.string.button_start_app, packageApp.appName), Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    companion object {
        @Singleton
        class Factory @Inject constructor(
                private val packageManager: PackageManager,
                private val packageUtils: PackageUtils,
                private val imageManager: ImageManager,
                private val notificationManager: NotificationManager) {

            fun getTrackedPackageAdapter(
                    context: Context,
                    managedPackage: List<PackageApp>,
                    onApplicationStarted: () -> Unit,
                    onRequestUpdate: () -> Unit
            ): TrackedPackageAdapter {
                return TrackedPackageAdapter(
                        packageManager,
                        packageUtils,
                        notificationManager,
                        imageManager,
                        context,
                        managedPackage,
                        onApplicationStarted,
                        onRequestUpdate
                )
            }
        }
    }
}