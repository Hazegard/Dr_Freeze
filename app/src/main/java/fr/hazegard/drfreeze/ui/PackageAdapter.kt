package fr.hazegard.drfreeze.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.hazegard.drfreeze.ImageManager
import fr.hazegard.drfreeze.R
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import kotlinx.android.synthetic.main.row_package.view.*
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * Created by Hazegard on 26/02/18.
 */

class PackageAdapter private constructor(
        private val imageManager: ImageManager,
        private var packages: List<PackageApp>,
        var trackedPackages: MutableMap<Pkg, PackageApp>,
        val packagesToAdd: MutableMap<Pkg, PackageApp>,
        val packagesToRemove: MutableMap<Pkg, PackageApp>,
        private val onUpdateList: () -> Unit
) : RecyclerView.Adapter<PackageAdapter.PackageHolder>() {

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
        packagesToAdd.clear()
        packagesToRemove.clear()
        notifyDataSetChanged()
    }

    inner class PackageHolder(private val view: View, private val c: Context) : RecyclerView.ViewHolder(view) {

        fun setContent(packageApp: PackageApp) {
            with(view) {
                row_package.setOnClickListener {
                    if (isEdit && packageApp.pkg.s != c.packageName) {
                        package_checkbox.isChecked = !package_checkbox.isChecked
                    }
                }
                packageNameTv.text = packageApp.pkg.s
                packageAppNameTv.text = packageApp.appName
                package_image.setImageDrawable(imageManager.getImage(packageApp))
            }
            with(view.package_checkbox) {
                setOnCheckedChangeListener(null)
                isChecked = trackedPackages.contains(packageApp.pkg)
                isEnabled = isEdit && packageApp.pkg.s != c.packageName
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        trackedPackages[packageApp.pkg] = packageApp
                        packagesToAdd[packageApp.pkg] = packageApp
                        packagesToRemove.remove(packageApp.pkg)
                    } else {
                        trackedPackages.remove(packageApp.pkg)
                        packagesToAdd.remove(packageApp.pkg)
                        packagesToRemove[packageApp.pkg] = packageApp
                    }
                    onUpdateList.invoke()
                }
            }
        }
    }

    companion object {
        class Factory @Inject constructor(private val imageManager: ImageManager) {

            private val packagesToAdd: MutableMap<Pkg, PackageApp> = mutableMapOf()

            fun addPackagesToAdd(packagesToAdd: MutableMap<Pkg, PackageApp>) {
                this.packagesToAdd.putAll(packagesToAdd)
            }

            private val packagesToRemove: MutableMap<Pkg, PackageApp> = mutableMapOf()
            fun addPackagesToRemove(packagesToRemove: MutableMap<Pkg, PackageApp>) {
                this.packagesToRemove.putAll(packagesToRemove)
            }

            fun get(packages: List<PackageApp>,
                    trackedPackages: MutableMap<Pkg, PackageApp>,
                    onUpdateList: () -> Unit
            ): PackageAdapter {
                return PackageAdapter(
                        imageManager,
                        packages,
                        trackedPackages,
                        packagesToAdd,
                        packagesToRemove,
                        onUpdateList
                )
            }
        }
    }
}