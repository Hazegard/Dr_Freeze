package fr.hazegard.freezator

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable

/**
 * Created by maxime on 04/03/18.
 */
class PackageUtils {
    companion object {
        private fun getPackageIconDrawable(context: Context, packageName: String): Drawable {
            return context.packageManager.getApplicationIcon(packageName)
                    ?: ColorDrawable(Color.TRANSPARENT)
        }

        fun getPackageName(context: Context, packageName: String): String {
            val appInfo = context.packageManager.getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA)
            return context.packageManager.getApplicationLabel(appInfo).toString()
        }

        fun getPackageIconBitmap(context: Context, packageName: String): Bitmap {
            return getBitmapFromDrawable(getPackageIconDrawable(context, packageName))
        }

        private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
            val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bmp
        }
    }
}