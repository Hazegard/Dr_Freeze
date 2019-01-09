package fr.hazegard.freezator.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.hazegard.freezator.PackageUtils
import fr.hazegard.freezator.R
import fr.hazegard.freezator.SharedPreferenceHelper
import kotlinx.android.synthetic.main.row_process.view.*

/**
 * Created by maxime on 26/02/18.
 */

class ProcessAdapter(context: Context, private var processNames: List<ApplicationInfo>) : RecyclerView.Adapter<ProcessAdapter.ProcessHolder>() {
    override fun onBindViewHolder(holder: ProcessHolder, position: Int) {
        val process: ApplicationInfo = processNames[position]
        holder.setContent(process)
    }

    var isEdit = false
        set(value) {
            field = value
            if (value) {
                isApplicationTrackedBak = HashMap(isApplicationTracked)
            }
            notifyDataSetChanged()
        }

    private var sp: SharedPreferenceHelper = SharedPreferenceHelper(context)

    private lateinit var isApplicationTrackedBak: MutableMap<String, Boolean>
    var isApplicationTracked: MutableMap<String, Boolean> = sp.getMapTrackedApplications()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_process, parent, false)
        return ProcessHolder(itemView, parent.context)
    }

    override fun getItemCount(): Int {
        return processNames.size
    }

    fun validateChange() {
        sp.saveTrackedApplication(isApplicationTracked)
    }

    fun cancelChange() {
        isApplicationTracked = isApplicationTrackedBak
    }

    inner class ProcessHolder(private val view: View, private val context: Context) : RecyclerView.ViewHolder(view) {

        fun setContent(process: ApplicationInfo) = with(view) {
            process_checkbox.setOnCheckedChangeListener { _, isChecked ->
                isApplicationTracked[process.processName] = isChecked
            }

            process_checkbox.isChecked = isApplicationTracked[process.processName] ?: false
            process_checkbox.isEnabled = isEdit
            processNameTv.text = PackageUtils.getPackageName(context, process.packageName)
            processAppNameTv.text = process.packageName
            process_image.setImageDrawable(PackageUtils.getPackageIconDrawable(context, process.packageName))
        }

        private fun getAppName(process: ApplicationInfo, context: Context): Pair<String, String> {
            return try {
                val appInfo: ApplicationInfo? = this.context.packageManager.getApplicationInfo(process.processName, 0)
                Pair(context.packageManager.getApplicationLabel(appInfo).toString(), process.processName)
            } catch (e: PackageManager.NameNotFoundException) {
                Pair("", process.processName)
            }
        }

        private fun getAppIcon(process: ApplicationInfo, context: Context): Drawable {
            return try {
                context.packageManager.getApplicationIcon(process.processName)
            } catch (e: PackageManager.NameNotFoundException) {
                ContextCompat.getDrawable(context, R.mipmap.ic_launcher)!!
            }
        }
    }
}