package com.njem.smartsms.data

import android.content.Context
import android.provider.Telephony
import com.njem.smartsms.data.model.SmsCategory
import com.njem.smartsms.data.model.SmsMessage

class SmsRepository(private val context: Context) {

    fun getAllSms(): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.READ
            ),
            null, null,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressCol = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyCol = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateCol = it.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val readCol = it.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (it.moveToNext()) {
                val body = it.getString(bodyCol) ?: ""
                val address = it.getString(addressCol) ?: ""
                messages.add(
                    SmsMessage(
                        id = it.getLong(idCol),
                        address = address,
                        body = body,
                        date = it.getLong(dateCol),
                        isRead = it.getInt(readCol) == 1,
                        category = categorize(body, address)
                    )
                )
            }
        }
        return messages
    }

    private fun categorize(body: String, address: String): SmsCategory {
        val lower = body.lowercase()
        return when {
            lower.contains(Regex("\\b\\d{4,8}\\b")) &&
            (lower.contains("code") || lower.contains("otp") ||
            lower.contains("verify") || lower.contains("uthibitisho") ||
            lower.contains("nambari yako")) -> SmsCategory.OTP

            lower.contains(Regex("(mpesa|tigo|airtel money|benki|bank|balance|tsh|tshs|credited|debited|transfer|imetumwa|umelipa|umelipwa)")) ->
            SmsCategory.BANK

            lower.contains(Regex("(winner|won|prize|umeshinda|zawadi|click|free|offer|deal|discount|promo)")) ->
            SmsCategory.SPAM

            lower.contains(Regex("(offer|sale|discount|shop|store|bei|punguzo|asilimia|%)")) ->
            SmsCategory.ADS

            else -> SmsCategory.PERSONAL
        }
    }

    fun getContactName(address: String): String {
        return try {
            val uri = android.net.Uri.withAppendedPath(
                android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                android.net.Uri.encode(address)
            )
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(0) ?: address
                } else address
            } ?: address
        } catch (e: Exception) {
            address
        }
    }
}
