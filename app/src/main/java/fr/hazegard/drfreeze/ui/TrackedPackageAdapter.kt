package fr.hazegard.drfreeze.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import fr.hazegard.drfreeze.ImageManager
import fr.hazegard.drfreeze.PackageUtils
import fr.hazegard.drfreeze.R
import fr.hazegard.drfreeze.model.PackageApp
import kotlinx.android.synthetic.main.row_manage_apps.view.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The adapter used to display tracked packages
 */
class TrackedPackageAdapter private constructor(
        val onClick: OnClick,
        private val packageUtils: PackageUtils,
        private val imageManager: ImageManager,
        private val c: Context,
        var managedPackage: MutableList<PackageApp>)
    : RecyclerView.Adapter<TrackedPackageAdapter.ManagedAppHolder>() {

    override fun onBindViewHolder(holder: ManagedAppHolder, position: Int) {
        val appName = managedPackage[position]
        holder.setContent(appName, position)
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
        managedPackage = packages.toMutableList()
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        managedPackage.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, managedPackage.size)
    }

    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }

    inner class ManagedAppHolder(private val view: View)
        : RecyclerView.ViewHolder(view) {

        /**
         * Set the content of a item
         * @param packageApp The package to be displayed
         */
        fun setContent(packageApp: PackageApp, position: Int) {
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
                        onClick.onAddShortCutCLick(position)
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, c.getString(R.string.button_add_shortcut, packageApp.appName), Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                }

                manage_freeze_app.setOnClickListener { }
                with(manage_freeze_app) {
                    if (isPkgEnabled) {
                        setOnClickListener {
                            onClick.onFreezeClick(position)
                        }
                        setImageResource(R.drawable.snowflake)
                    } else {
                        setOnClickListener {
                            onClick.onUnfreezeClick(position)
                        }
                        setImageResource(R.drawable.fire)
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, context.getString(R.string.button_freeze_app, packageApp.appName), Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                    isEnabled = isPackageInstalled
                }

                with(manage_untrack_app) {
                    setOnClickListener {
                        onClick.onUntrackClick(position)
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
                            onClick.onClickStartApplication(position)
                        } else {
                            Toast.makeText(context, context.getString(R.string.application_not_found), Toast.LENGTH_LONG).show()
                        }
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, context.getString(R.string.button_start_app, packageApp.appName), Toast.LENGTH_SHORT).show()
                        return@setOnLongClickListener true
                    }
                }
                with(switch_show_notifications) {
                    setOnCheckedChangeListener(null)
                    isChecked = packageApp.doNotify
                    setOnCheckedChangeListener { _, isChecked ->
                        onClick.onNotificationSwitchClick(position, isChecked)
                        packageApp.doNotify = isChecked
                    }
                    setOnLongClickListener {
                        Toast.makeText(context, context.getString(R.string.switch_notification_status, packageApp.appName), Toast.LENGTH_LONG).show()
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    companion object {
        @Singleton
        class Factory @Inject constructor(
                private val packageUtils: PackageUtils,
                private val imageManager: ImageManager) {

            fun getTrackedPackageAdapter(
                    context: Context,
                    OnClick: OnClick,
                    managedPackage: MutableList<PackageApp>
            ): TrackedPackageAdapter {
                return TrackedPackageAdapter(
                        OnClick,
                        packageUtils,
                        imageManager,
                        context,
                        managedPackage
                )
            }
        }
    }

    interface OnClick {
        fun onAddShortCutCLick(position: Int)
        fun onFreezeClick(position: Int)
        fun onUnfreezeClick(position: Int)
        fun onUntrackClick(position: Int)
        fun onNotificationSwitchClick(position: Int, newState: Boolean)
        fun onClickStartApplication(position: Int)
    }
}