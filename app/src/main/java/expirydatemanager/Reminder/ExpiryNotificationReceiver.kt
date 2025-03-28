package nithra.tamil.calendar.expirydatemanager.Notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import expirydatemanager.activity.AddItemActivity
import nithra.tamil.calendar.expirydatemanager.R

class ExpiryNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val itemName = intent?.getStringExtra("itemName") ?: "Item"
        val notificationId = intent?.getIntExtra("notificationId", 0) ?: 0
        val expiryDate = intent?.getStringExtra("expiryDate") ?: "N/A"  // now in dd_MM_yyyy
        val notifyTime = intent?.getStringExtra("notifyTime") ?: ""

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "expiry_notification_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Expiry Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open AddItemActivity
        val openIntent = Intent(context, AddItemActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Highlight expiry date using bold styling
        val message = "🕒 Your item <b>$itemName</b> is expiring on <b><font color='#FF0000'>$expiryDate</font></b>"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.expity_nodata)
            .setContentTitle("📦 Expiry Reminder")
            .setContentText("Your item is expiring soon")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_LEGACY)
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }


}
