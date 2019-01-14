package fr.hazegard.freezator.extensions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

/**
 * Convert the drawable to Bitmap
 * @return The bitmap from the drawable
 */
fun Drawable.toBitmap(): Bitmap {
    val bmp = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)
    return bmp
}