package com.njem.smartsms.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.njem.smartsms.MainActivity
import com.njem.smartsms.data.model.SmsCategory

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { smsMessage ->
                val sender = smsMessage.originatingAddress ?: "Unknown"
                val body = smsMessage.messageBody ?: ""
                val category = categorize(body)
                showNotification(context, sender, body, category)
                context.sendBroadcast(Intent("com.njem.smartsms.SMS_RECEIVED"))
            }
        }
    }

    private fun showNotification(context: Context, sender: String, body: String, category: SmsCategory) {
        val channelId = "smart_sms_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Smart SMS AI", NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = when (category) {
            SmsCategory.BANK -> "💰"
            SmsCategory.OTP -> "🔑"
            SmsCategory.SPAM -> "🚫"
            SmsCategory.ADS -> "📢"
            SmsCategory.PERSONAL -> "👤"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("$emoji $sender")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun categorize(body: String): SmsCategory {
        val lower = body.lowercase()
        return when {
            lower.contains(Regex("\\b\\d{4,8}\\b")) &&
            (lower.contains("code") || lower.contains("otp") ||
            lower.contains("verify") || lower.contains("uthibitisho")) -> SmsCategory.OTP
            lower.contains(Regex("(mpesa|tigo|bank|balance|tsh|tshs|credited|debited|imetumwa|selcom|songesha)")) -> SmsCategory.BANK
            lower.contains(Regex("(winner|won|umeshinda|zawadi|free|promo)")) -> SmsCategory.SPAM
            lower.contains(Regex("(offer|sale|discount|punguzo|%)")) -> SmsCategory.ADS
            else -> SmsCategory.PERSONAL
        }
    }
}
