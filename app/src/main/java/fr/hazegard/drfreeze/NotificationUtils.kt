package fr.hazegard.drfreeze

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import fr.hazegard.drfreeze.model.PackageApp
import fr.hazegard.drfreeze.model.Pkg


/**
 * Created by Hazegard on 04/03/18.
 */
class NotificationUtils {
    companion object {

        /**
         * Send multiple notifications displaying that the packages are currently running
         * Allowing the user to disable the packages by clicking on the notification
         * @param context The current context
         * @param packages THe packages targeted by the notifications
         */
        fun sendNotification(context: Context, packages: List<PackageApp>) {
            val isPersistent = PreferencesHelper.isNotificationPersistent(context)
            packages.forEach {
                sendNotification(context, it, isPersistent)
            }
        }

        /**
         * Send a notification displaying that the package is currently running
         * Allowing the user to disable the package by clicking on the notification
         * @param context The current context
         * @param pkg THe package targeted by the notification
         */
        fun sendNotification(context: Context, pkg: PackageApp) {
            val isPersistent = PreferencesHelper.isNotificationPersistent(context)
            sendNotification(context, pkg, isPersistent)
        }

        /**
         * Send a notification displaying that the package is currently running
         * Allowing the user to disable the package by clicking on the notification
         * @param context The current context
         * @param packageApp THe package targeted by the notification
         * @param isPersistent Whether the notification should be persistent
         */
        private fun sendNotification(context: Context, packageApp: PackageApp, isPersistent: Boolean) {
            if (PreferencesHelper.isNotificationDisabled(context)) {
                return
            }

            val onClickIntent = NotificationActionService.newDisablePackageIntent(context, packageApp.appName)
            val pendingIntent: PendingIntent = PendingIntent.getService(
                    context, System.currentTimeMillis().toInt(), onClickIntent, PendingIntent.FLAG_ONE_SHOT)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "1664"
            val name = context.getString(R.string.channel_name)
            val description = context.getString(R.string.channel_description)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_LOW
                val channel = NotificationChannel(channelId, name, importance)
                channel.description = description
                channel.enableLights(false)
                channel.enableVibration(false)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(packageApp.appName)
                    .setContentText("Click to disable ${packageApp.appName}")
                    .setLargeIcon(packageApp.getIconBitmap(context))
                    .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        R.drawable.snowflake
                    } else {
                        R.drawable.snowflake_compat
                    })
                    .setOngoing(isPersistent)
                    .setAutoCancel(true)
                    .build()
            val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(packageApp.pkg.hashCode(), notification)
        }

        /**
         * Remove a notification
         * @param context The current context
         * @param pkg THe package targeted by the notification that should be removed
         */
        fun removeNotification(context: Context, pkg: Pkg) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(pkg.s.hashCode())
        }

        /**
         * The class that handle intent send by notifications
         * The intent actions are:
         *  - ACTION_DISABLE: Disable the package given as extra
         */
        class NotificationActionService : IntentService("NotificationActionService") {
            private val appsManager by lazy { PackageManager(this@NotificationActionService) }
            override fun onHandleIntent(intent: Intent?) {
                val action = intent?.action
                if (action.equals(ACTION_DISABLE)) {
                    intent?.extras?.getString(KEY_PACKAGE, null)?.let {
                        appsManager.disablePackage(Pkg(it))
                    }
                }
            }

            companion object {
                private const val ACTION_DISABLE = "ACTION_DISABLE"
                private const val KEY_PACKAGE = "KAY_PACKAGE"
                /**
                 * Create a new Intent that disable the package
                 * @param context The current context
                 * @param packageName The package tha tshould be disabled
                 */
                fun newDisablePackageIntent(context: Context, packageName: String): Intent {
                    return Intent(context, NotificationActionService::class.java).apply {
                        action = ACTION_DISABLE
                        putExtra(KEY_PACKAGE, packageName)
                    }
                }
            }
        }
    }
}