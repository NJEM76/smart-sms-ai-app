package com.njem.smartsms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.njem.smartsms.data.model.SmsCategory
import com.njem.smartsms.data.model.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { smsMessage ->
                val sender = smsMessage.originatingAddress ?: ""
                val body = smsMessage.messageBody ?: ""
                val category = categorize(body)
            }
        }
    }

    private fun categorize(body: String): SmsCategory {
        val lower = body.lowercase()
        return when {
            lower.contains(Regex("\\b\\d{4,8}\\b")) &&
            (lower.contains("code") || lower.contains("otp") ||
            lower.contains("verify")) -> SmsCategory.OTP

            lower.contains(Regex("(mpesa|tigo|airtel|benki|bank|balance|tsh|tshs|credited|debited|transfer)")) ->
            SmsCategory.BANK

            lower.contains(Regex("(winner|won|prize|click|free|offer|deal|discount|promo|buy now)")) ->
            SmsCategory.SPAM

            lower.contains(Regex("(offer|sale|discount|shop|store|bei|punguzo)")) ->
            SmsCategory.ADS

            else -> SmsCategory.PERSONAL
        }
    }
}
