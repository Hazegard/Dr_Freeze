package fr.hazegard.drfreeze

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import fr.hazegard.drfreeze.extensions.toBitmap
import fr.hazegard.drfreeze.model.PackageApp
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageManager @Inject constructor(
        private val context: Context,
        private val pm: PackageManager) {

    fun saveImage(packageApp: PackageApp, image: Drawable) {
        val imagePath = getFilePath(packageApp)
        val fos = FileOutputStream(imagePath)
        try {
            image.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCachedImage(packageApp: PackageApp): Drawable {
        val image = getImageFromPacMan(packageApp)
        if (image != null) {
            return image
        }
        val imageDrawable: Drawable? = pm.getApplicationIcon(packageApp.pkg.s) ?: null
        if (imageDrawable != null) {
            saveImage(packageApp, imageDrawable)
            return imageDrawable
        }
        return defaultIcon()
    }

    private fun getImageFromPacMan(packageApp: PackageApp): Drawable? {
        return pm.getApplicationIcon(packageApp.pkg.s)
    }

    fun getImage(packageApp: PackageApp): Drawable {
        return getImageFromPacMan(packageApp) ?: defaultIcon()
    }

    private fun defaultIcon(): Drawable = ColorDrawable(Color.TRANSPARENT)

    fun getCachedImage0(packageApp: PackageApp): Bitmap? {
        return try {
            val imagePath = getFilePath(packageApp)
            val inputStream = FileInputStream(imagePath)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    fun deleteImage(packageApp: PackageApp) {
        try {
            val imagePath = getFilePath(packageApp)
            imagePath.delete()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getFilePath(packageApp: PackageApp): File {
        val imageDir = context.getDir(IMAGE_FOLDER, Context.MODE_PRIVATE)
        return File(imageDir, packageApp.pkg.s)
    }

    companion object {
        private const val IMAGE_FOLDER = "applications_images"
    }
}