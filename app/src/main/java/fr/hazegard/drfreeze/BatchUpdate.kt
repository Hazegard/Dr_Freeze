package fr.hazegard.drfreeze

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.hazegard.drfreeze.repository.DbWrapper
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class BatchUpdate @Inject constructor(
        private val dbWrapper: DbWrapper,
        @Named("Shared_preferences") private val sharedPreferences: SharedPreferences,
        private val packageUtils: PackageUtils,
        private val packageManager: PackageManager,
        private val context: Context) {

    fun enableUpdateMode() {
        val disabledPackages = packageManager.getDisabledInstalledAndTracked()
        sharedPreferences.edit().putBoolean(UPDATE_MODE, true).apply()
        disabledPackages.forEach { packageApp ->
            packageUtils.enablePackage(packageApp.pkg, false)
            dbWrapper.setFlagUpdate(packageApp)
        }
        sendBatchNotification()
    }

    fun isUpdateModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(UPDATE_MODE, false)
    }

    fun disableUpdateMode() {
        val flaggedPackages = dbWrapper.selectFlaggedUpdatePackages()
        flaggedPackages.forEach { packageApp ->
            packageUtils.disablePackage(packageApp.pkg)
            dbWrapper.resetFlagUpdate(packageApp)
        }
        removeNotification()
        sharedPreferences.edit().remove(UPDATE_MODE).apply()
    }

    fun sendBatchNotification() {
        val onClickIntent = StopBatchUpdateService.newIntent(context)
        val pendingIntent: PendingIntent = PendingIntent.getService(
                context, System.currentTimeMillis().toInt(), onClickIntent, PendingIntent.FLAG_ONE_SHOT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = BATCH_UPDATE_CHANNEL_ID
        val name = context.getString(R.string.channel_name)
        val descriptionChannel = context.getString(R.string.channel_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = android.app.NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionChannel
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
                .setContentIntent(pendingIntent)
                .setContentTitle("Update Enabled")
                .setContentText("Click to disable the update mode")
//                .setLargeIcon(imageManager.getCachedImage(packageApp).toBitmap())
                .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    R.drawable.snowflake
                } else {
                    R.drawable.snowflake_compat
                })
                .setOngoing(true)
                .setAutoCancel(true)
                .build()
        val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(ID, notification)
    }

    fun removeNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ID)
    }

    companion object {
        private const val ID = 1665
        private const val UPDATE_MODE = "UPDATE_MODE"
        private const val BATCH_UPDATE_CHANNEL_ID = "1665"
    }
}