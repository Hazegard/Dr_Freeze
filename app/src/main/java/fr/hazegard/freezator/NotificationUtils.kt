package fr.hazegard.freezator

import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.graphics.drawable.Drawable
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import fr.hazegard.freezator.ui.MainActivity


/**
 * Created by maxime on 04/03/18.
 */
class NotificationUtils {
    companion object {
        fun notify(context: Context, packageName: String) {

            val appName = PackageUtils.getPackageName(context, packageName)
            val appIconBitmap: Bitmap = PackageUtils.getPackageIconBitmap(context, packageName)

            val onClickIntent = NotificationActionService.newIntent(context, packageName)
//            val onClickIntent = Intent(context,MainActivity::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getService(
                    context, System.currentTimeMillis().toInt(), onClickIntent, PendingIntent.FLAG_ONE_SHOT)

            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "1664"
            val name = context.getString(R.string.channel_name)
            val description = context.getString(R.string.channel_description)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_LOW;
                val mChannel = NotificationChannel(channelId, name, importance)
                mChannel.description = description
                mChannel.enableLights(false)
                mChannel.enableVibration(false)
                mNotificationManager.createNotificationChannel(mChannel)
            }
            val notification = NotificationCompat.Builder(context, channelId)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(appName)
                    .setContentText("Click to disable $appName")
                    .setLargeIcon(appIconBitmap)
                    .setSmallIcon(R.drawable.snowflake)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .build()
            val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.notify(packageName.hashCode(), notification)
        }

        class NotificationActionService : IntentService("NotificationActionService") {
            private val appsManager by lazy { AppsManager(this@NotificationActionService) }
            override fun onHandleIntent(intent: Intent?) {
                val packageName: String = intent?.action ?: "null"
                Log.d("NotifCLick", packageName)
                val res = appsManager.disablePackage(packageName)
                Log.d("NotifClick", res)
            }

            companion object {
                fun newIntent(context: Context, packageName: String): Intent {
                    val intent = Intent(context, NotificationActionService::class.java)
                    intent.action = packageName
                    return intent
                }
            }
        }
    }
}