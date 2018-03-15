package fr.hazegard.freezator

import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log

/**
 * Created by maxime on 04/03/18.
 */
class NotificationUtils(private val context: Context) {
    fun notify(packageName: String) {

        val appName = PackageUtils.getPackageName(context, packageName)
        val appIconBitmap: Bitmap = (PackageUtils.getPackageIcon(context,
                packageName) as BitmapDrawable).bitmap

        val onClickIntent = Intent(context, NotificationActionService::class.java)
        onClickIntent.action = packageName
        val pendingIntent: PendingIntent = PendingIntent.getService(
                context, 0, onClickIntent, PendingIntent.FLAG_ONE_SHOT)

        val notification = NotificationCompat.Builder(context, "test")
                .setContentTitle(appName)
                .setContentText(packageName)
                .setLargeIcon(appIconBitmap)
                .setSmallIcon(R.drawable.snowflake)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(packageName.hashCode(), notification)
    }

    companion object {
        class NotificationActionService : IntentService("NotificationActionService") {
            private val appsManager by lazy { AppsManager(this@NotificationActionService) }
            override fun onHandleIntent(intent: Intent?) {
                val packageName: String = intent?.action ?: "null"
                Log.d("NotifCLick", packageName)
                val res = appsManager.enablePackage(packageName)
                Log.d("NotifClick", res)
            }

        }
    }
}