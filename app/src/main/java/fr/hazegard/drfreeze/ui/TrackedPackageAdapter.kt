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
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.Delegates

/**
 * The adapter used to display tracked packages
 */
class TrackedPackageAdapter private constructor(
        val onClick: OnClick,
        private val packageUtils: PackageUtils,
        private val batchUpdate: BatchUpdate,
        private val imageManager: ImageManager,
        private val preferencesHelper: PreferencesHelper,
        private val c: Context,
        var managedPackage: MutableList<PackageApp>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isUpdateModeEnabled by Delegates.observable(batchUpdate.isUpdateModeEnabled()) { _, _, newValue: Boolean ->
        headerOffset = if (newValue) {
            1
        } else {
            0
        }
    }

    private var headerOffset: Int = if (isUpdateModeEnabled) {
        1
    } else {
        0
    }
    private var isNotificationsDisabled = preferencesHelper.isNotificationDisabled()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderHolder -> {
                holder.setHeader()
            }
            is ManagedAppHolder -> {
                val appName = managedPackage[position - headerOffset]
                holder.setContent(appName, position - headerOffset)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            val itemView: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_manage_apps, parent, false)
            ManagedAppHolder(itemView)
        } else {
            val itemView: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_update_mode, parent, false)
            HeaderHolder(itemView)
        }
    }

    override fun getItemCount(): Int {
        return managedPackage.size + headerOffset
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && isUpdateModeEnabled) {
            HEADER
        } else {
            ITEM
        }
    }

    /**
     * Update the packages of packages and update the ui
     * @param packages The new list of packages
     */
    fun updateList(packages: List<PackageApp>) {
        isNotificationsDisabled = preferencesHelper.isNotificationDisabled()
        managedPackage = packages.toMutableList()
        notifyDataSetChanged()
    }

    /**
     * Remove the item from the list and update the view
     * @param position THe position of the item to remove
     */
    fun removeAt(position: Int) {
        managedPackage.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, managedPackage.size)
    }

    /**
     * Update an item
     * @param position The position to update
     */
    fun updateItem(position: Int) {
        val packagePosition = position + headerOffset
        notifyItemChanged(packagePosition)
    }

    /**
     * Update the view to show the header if needed
     */
    fun updateHeader() {
        isUpdateModeEnabled = batchUpdate.isUpdateModeEnabled()
        if (isUpdateModeEnabled) {
            notifyItemInserted(0)
        } else {
            notifyItemRemoved(0)
        }
    }

    inner class HeaderHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun setHeader() {
            with(view) {
                setOnClickListener {
                    onClick.onClickStopBatchUpdate()
                }
                setOnLongClickListener {
                    Toast.makeText(c, "Stop batch Update", Toast.LENGTH_LONG).show()
                    return@setOnLongClickListener true
                }
            }
        }
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
                    if (isNotificationsDisabled) {
                        isChecked = false
                        isClickable = false
                    } else {
                        isChecked = packageApp.doNotify
                    }
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isNotificationsDisabled) {
                            this.isChecked = false
                        } else {
                            onClick.onNotificationSwitchClick(position, isChecked)
                            packageApp.doNotify = isChecked
                        }
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
        const val ITEM = 1
        const val HEADER = 0

        @Singleton
        class Factory @Inject constructor(
                private val packageUtils: PackageUtils,
                private val batchUpdate: BatchUpdate,
                private val preferencesHelper: PreferencesHelper,
                private val imageManager: ImageManager) {

            fun getTrackedPackageAdapter(
                    context: Context,
                    OnClick: OnClick,
                    managedPackage: MutableList<PackageApp>
            ): TrackedPackageAdapter {
                return TrackedPackageAdapter(
                        OnClick,
                        packageUtils,
                        batchUpdate,
                        imageManager,
                        preferencesHelper,
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
        fun onClickStopBatchUpdate()
    }
}