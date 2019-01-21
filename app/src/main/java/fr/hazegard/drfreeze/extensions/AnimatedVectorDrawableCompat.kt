package fr.hazegard.drfreeze.extensions

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.support.graphics.drawable.Animatable2Compat
import android.support.graphics.drawable.AnimatedVectorDrawableCompat

/**
 * Set Callback when the animation finish
 * @param callback The callback to execute
 */
fun Animatable.onAnimationEnd(callback: () -> Unit) {
    AnimatedVectorDrawableCompat.registerAnimationCallback(
            this as Drawable,
            object : Animatable2Compat.AnimationCallback() {
                val handler = Handler(Looper.getMainLooper())
                override fun onAnimationEnd(drawable: Drawable?) {
                    handler.post { callback() }
                }
            })
}
