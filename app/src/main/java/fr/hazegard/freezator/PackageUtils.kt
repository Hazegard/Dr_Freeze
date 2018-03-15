package fr.hazegard.freezator

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable

/**
 * Created by maxime on 04/03/18.
 */
class PackageUtils {
    companion object {
        fun getPackageIcon(context: Context, packageName: String): Drawable {
            return context.packageManager.getApplicationIcon(packageName)
                    ?: ColorDrawable(Color.TRANSPARENT)
        }

        fun getPackageName(context: Context, packageName: String): String {
            val appInfo =  context.packageManager.getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA)
            return context.packageManager.getApplicationLabel(appInfo).toString()
        }
    }
}