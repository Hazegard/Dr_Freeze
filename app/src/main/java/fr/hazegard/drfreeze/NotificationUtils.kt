package fr.hazegard.drfreeze

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.android.AndroidInjection
import fr.hazegard.drfreeze.extensions.toBitmap
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg
import javax.inject.Inject


/**
 * Created by Hazegard on 04/03/18.
 */

class NotificationUtils @Inject constructor(private val context: Context,
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
        val channelId = "1664"
        val name = context.getString(R.string.channel_name)
        val descr = context.getString(R.string.channel_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descr
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
                .setContentIntent(pendingIntent)
                .setContentTitle(packageApp.appName)
                .setContentText("Click to disable ${packageApp.appName}")
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
        /**
         * The class that handle intent send by notifications
         * The intent actions are:
         *  - ACTION_DISABLE: Disable the package given as extra
         */
        class NotificationActionService : IntentService("NotificationActionService") {
            @Inject
            lateinit var packageUtils: PackageUtils

            override fun onHandleIntent(intent: Intent?) {
                AndroidInjection.inject(this)
                val action = intent?.action
                if (action.equals(ACTION_DISABLE)) {
                    intent?.extras?.getString(KEY_PACKAGE, null)?.let {
                        packageUtils.disablePackage(Pkg(it))
                    }
                }
            }

            companion object {
                private const val ACTION_DISABLE = "ACTION_DISABLE"
                private const val KEY_PACKAGE = "KAY_PACKAGE"
                /**
                 * Create a new Intent that disable the package
                 * @param context The current context
                 * @param pkg The package that should be disabled
                 */
                fun newDisablePackageIntent(context: Context, pkg: Pkg): Intent {
                    return Intent(context, NotificationActionService::class.java).apply {
                        action = ACTION_DISABLE
                        putExtra(KEY_PACKAGE, pkg.s)
                    }
                }
            }
        }
    }
}