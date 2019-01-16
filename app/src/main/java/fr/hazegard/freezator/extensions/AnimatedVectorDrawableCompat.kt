package fr.hazegard.freezator.extensions

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.support.graphics.drawable.Animatable2Compat
import android.support.graphics.drawable.AnimatedVectorDrawableCompat


fun AnimatedVectorDrawableCompat.onAnimationEnd(callback: () -> Unit) {
    registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
        val handler = Handler(Looper.getMainLooper())
        override fun onAnimationEnd(drawable: Drawable?) {
            handler.post { callback() }
        }
    })
}
