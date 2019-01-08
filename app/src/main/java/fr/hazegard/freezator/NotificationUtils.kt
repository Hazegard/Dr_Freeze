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


/**
 * Created by maxime on 04/03/18.
 */
class NotificationUtils {


    companion object {
        private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
            val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bmp
        }

        fun notify(context: Context, packageName: String) {

            val appName = PackageUtils.getPackageName(context, packageName)
            val appIconBitmap: Bitmap = getBitmapFromDrawable(PackageUtils.getPackageIcon(context,
                    packageName))

            val onClickIntent = Intent(context, NotificationActionService::class.java)
            onClickIntent.action = packageName
            val pendingIntent: PendingIntent = PendingIntent.getService(
                    context, 0, onClickIntent, PendingIntent.FLAG_ONE_SHOT)

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
                    .setContentTitle(appName)
                    .setContentText("Click to disable $appName")
                    .setLargeIcon(appIconBitmap)
                    .setSmallIcon(R.drawable.snowflake)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
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
                val res = appsManager.enablePackage(packageName)
                Log.d("NotifClick", res)
            }

        }
    }
}