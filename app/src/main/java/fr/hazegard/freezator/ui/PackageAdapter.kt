package fr.hazegard.freezator.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.hazegard.freezator.PackageApp
import fr.hazegard.freezator.PackageManager
import fr.hazegard.freezator.R
import kotlinx.android.synthetic.main.row_package.view.*

/**
 * Created by Hazegard on 26/02/18.
 */

class PackageAdapter(context: Context,
                     private var packages: List<PackageApp>,
                     private val onUpdateList: () -> Unit)
    : RecyclerView.Adapter<PackageAdapter.PackageHolder>() {
    override fun onBindViewHolder(holder: PackageHolder, position: Int) {
        val pkg: PackageApp = packages[position]
        holder.setContent(pkg)
    }

    var isEdit = false
        set(value) {
            field = value
            if (value) {
                trackedPackagesBak = trackedPackages.toMutableSet()
            }
            notifyDataSetChanged()
        }
    private val appsManager = PackageManager(context)

    private var trackedPackages: MutableSet<String> = appsManager.getTrackedPackagesAsSet().toMutableSet()
    private lateinit var trackedPackagesBak: MutableSet<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_package, parent, false)
        return PackageHolder(itemView, parent.context)
    }

    override fun getItemCount(): Int {
        return packages.size
    }

    fun validateChange() {
        appsManager.saveTrackedPackages(trackedPackages)
    }

    fun cancelChange() {
        trackedPackages = trackedPackagesBak
    }

    fun updateList(newPackageName: List<PackageApp>) {
        packages = newPackageName
        notifyDataSetChanged()
    }

    inner class PackageHolder(private val view: View, private val c: Context) : RecyclerView.ViewHolder(view) {

        fun setContent(pkg: PackageApp) {
            with(view) {
                package_checkbox.setOnCheckedChangeListener { _, isChecked ->
                    with(trackedPackages) {
                        if (isChecked) {
                            add(pkg.packageName)
                        } else {
                            remove(pkg.packageName)
                        }
                    }
                    onUpdateList()
                }

                row_package.setOnClickListener {
                    if (isEdit) {
                        package_checkbox.isChecked = !package_checkbox.isChecked
                    }
                }
                with(package_checkbox) {
                    isChecked = trackedPackages.contains(pkg.packageName)
                    isEnabled = isEdit && pkg.packageName != c.packageName
                }
                packageNameTv.text = pkg.packageName
                packageAppNameTv.text = pkg.appName
                package_image.setImageDrawable(pkg.getIconDrawable(c))
            }
        }
    }
}