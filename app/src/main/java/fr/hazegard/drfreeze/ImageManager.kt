package fr.hazegard.drfreeze

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import fr.hazegard.drfreeze.extensions.toBitmap
import fr.hazegard.drfreeze.model.PackageApp
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageManager @Inject constructor(
        private val context: Context,
        private val pm: PackageManager) {

    /**
     * Save the icon of package into internal storage
     * @param packageApp THe package to save
     * @param image The icon of the package to save
     */
    private fun saveImage(packageApp: PackageApp, image: Drawable) {
        try {
            val imagePath = getFilePath(packageApp)
            val fos = FileOutputStream(imagePath)
            image.toBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get the image from cache if it exists
     * @return THe image or null if not found
     */
    private fun getImageFromCached(packageApp: PackageApp): Drawable? {
        return try {
            val imagePath = getFilePath(packageApp)
            val fis = FileInputStream(imagePath)
            BitmapDrawable(context.resources, BitmapFactory.decodeStream(fis))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get the image cached in the internal storage.
     * If the image is not found, the image is load from the system and saved to the internal storage
     * If nothing is found, a default icon is provided
     * @param packageApp The package of the icon
     * @return The icon of the package
     */
    fun getCachedImage(packageApp: PackageApp): Drawable {
        val image = getImageFromCached(packageApp)
        if (image != null) {
            return image
        }
        val imageDrawable: Drawable? = try {
            pm.getApplicationIcon(packageApp.pkg.s)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        if (imageDrawable != null) {
            saveImage(packageApp, imageDrawable)
            return imageDrawable
        }
        return defaultIcon()
    }

    /**
     * Get the icon of an application from the system
     * @param packageApp The package
     * @return The icon of the application
     */
    private fun getImageFromPacMan(packageApp: PackageApp): Drawable? {
        return pm.getApplicationIcon(packageApp.pkg.s)
    }

    /**
     * Get the icon from the system, and provide a default icon if not found
     */
    fun getImage(packageApp: PackageApp): Drawable {
        return getImageFromPacMan(packageApp) ?: defaultIcon()
    }

    /**
     * Generate a default icon
     */
    private fun defaultIcon(): Drawable = ColorDrawable(Color.TRANSPARENT)

    /**
     * Delete the icon from internal storage
     * @param packageApp The package to delete
     */
    fun deleteImage(packageApp: PackageApp) {
        try {
            val imagePath = getFilePath(packageApp)
            imagePath.delete()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get the path of the file in the internal storage
     */
    private fun getFilePath(packageApp: PackageApp): File {
        val imageDir = context.cacheDir
        return File(imageDir, packageApp.pkg.s)
    }

    fun updateImage(packageApp: PackageApp) {
        val image = getImageFromPacMan(packageApp) ?: return
        saveImage(packageApp, image)
    }

    fun updateImages(packages: List<PackageApp>) {
        packages.forEach { updateImage(it) }
    }

    companion object {
        private const val IMAGE_FOLDER = "applications_images"
    }
}