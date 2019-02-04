package fr.hazegard.drfreeze

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.hazegard.drfreeze.extensions.toBitmap
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import javax.inject.Inject


/**
 * Created by Hazegard on 04/03/18.
 */

class NotificationManager @Inject constructor(private val context: Context,
                                              private val preferencesHelper: PreferencesHelper,
                                              private val imageManager: ImageManager) {

    /**
     * Send multiple notifications displaying that the packages are currently running
     * Allowing the user to disable the packages by clicking on the notification
     * @param packages THe packages targeted by the notifications
     */
    fun sendNotification(packages: List<PackageApp>) {
        val isPersistent = preferencesHelper.isNotificationPersistent()
        packages.forEach {
            sendNotification(it, isPersistent)
        }
    }

    /**
     * Send a notification displaying that the package is currently running
     * Allowing the user to disable the package by clicking on the notification
     * @param pkg THe package targeted by the notification
     */
    fun sendNotification(pkg: PackageApp) {
        val isPersistent = preferencesHelper.isNotificationPersistent()
        sendNotification(pkg, isPersistent)
    }

    /**
     * Send a notification displaying that the package is currently running
     * Allowing the user to disable the package by clicking on the notification
     * @param packageApp THe package targeted by the notification
     * @param isPersistent Whether the notification should be persistent
     */
    private fun sendNotification(packageApp: PackageApp, isPersistent: Boolean) {
        if (preferencesHelper.isNotificationDisabled()) {
            return
        }

        val onClickIntent = NotificationActionService.newDisablePackageIntent(context, packageApp.pkg)
        val pendingIntent: PendingIntent = PendingIntent.getService(
                context, System.currentTimeMillis().toInt(), onClickIntent, PendingIntent.FLAG_ONE_SHOT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = NOTIFICATION_CHANNEL_ID
        val name = context.getString(R.string.channel_name)
        val descriptionChannel = context.getString(R.string.channel_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionChannel
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
                .setContentIntent(pendingIntent)
                .setContentTitle(packageApp.appName)
                .setContentText(context.getString(R.string.notification_click_action, packageApp.appName))
                .setLargeIcon(imageManager.getCachedImage(packageApp).toBitmap())
                .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    R.drawable.snowflake
                } else {
                    R.drawable.snowflake_compat
                })
                .setOngoing(isPersistent)
                .setAutoCancel(true)
                .build()
        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(packageApp.pkg.s.hashCode(), notification)
    }

    /**
     * Remove a notification
     * @param packageApp THe package targeted by the notification that should be removed
     */
    fun removeNotification(packageApp: PackageApp) {
        removeNotification(packageApp.pkg)
    }

    fun removeNotification(pkg: Pkg) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(pkg.s.hashCode())
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "1664"
    }
}