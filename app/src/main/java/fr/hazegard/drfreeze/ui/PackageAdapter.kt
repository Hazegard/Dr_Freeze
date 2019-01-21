package fr.hazegard.drfreeze.ui

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.hazegard.drfreeze.R
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import kotlinx.android.synthetic.main.row_package.view.*
import kotlin.properties.Delegates

/**
 * Created by Hazegard on 26/02/18.
 */

class PackageAdapter(private var packages: List<PackageApp>,
                     var trackedPackages: MutableSet<Pkg>,
                     private val onUpdateList: () -> Unit)
    : androidx.recyclerview.widget.RecyclerView.Adapter<PackageAdapter.PackageHolder>() {
    override fun onBindViewHolder(holder: PackageHolder, position: Int) {
        val pkg: PackageApp = packages[position]
        holder.setContent(pkg)
    }

    var isEdit by Delegates.observable(false) { _, _, _ ->
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageHolder {
        val itemView: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_package, parent, false)
        return PackageHolder(itemView, parent.context)
    }

    override fun getItemCount(): Int {
        return packages.size
    }

    fun updateList(newPackageName: List<PackageApp>) {
        packages = newPackageName
        notifyDataSetChanged()
    }

    inner class PackageHolder(private val view: View, private val c: Context) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        fun setContent(packageApp: PackageApp) {
            with(view) {
                package_checkbox.setOnCheckedChangeListener { _, isChecked ->
                    with(trackedPackages) {
                        if (isChecked) {
                            add(packageApp.pkg)
                        } else {
                            remove(packageApp.pkg)
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
                    isChecked = trackedPackages.contains(packageApp.pkg)
                    isEnabled = isEdit && packageApp.pkg.s != c.packageName
                }
                packageNameTv.text = packageApp.pkg.s
                packageAppNameTv.text = packageApp.appName
                package_image.setImageDrawable(packageApp.getIconDrawable(c))
            }
        }
    }
}