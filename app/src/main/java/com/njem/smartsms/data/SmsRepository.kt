package com.njem.smartsms.data

import android.content.Context
import android.provider.ContactsContract
import android.provider.Telephony
import com.njem.smartsms.data.model.Conversation
import com.njem.smartsms.data.model.SmsCategory
import com.njem.smartsms.data.model.SmsMessage

class SmsRepository(private val context: Context) {

    private fun loadAllContacts(): Map<String, String> {
        val contacts = mutableMapOf<String, String>()
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ),
                null, null, null
            )
            cursor?.use {
                val numberCol = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val nameCol = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                while (it.moveToNext()) {
                    val number = it.getString(numberCol)?.replace(" ", "")?.replace("-", "") ?: continue
                    val name = it.getString(nameCol) ?: continue
                    contacts[number] = name
                }
            }
        } catch (e: Exception) { }
        return contacts
    }

    private fun findContactName(address: String, contacts: Map<String, String>): String {
        val clean = address.replace(" ", "").replace("-", "")
        contacts[clean]?.let { return it }
        contacts.entries.forEach { (number, name) ->
            if (number.endsWith(clean) || clean.endsWith(number)) return name
        }
        return address
    }

    fun getAllSms(): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        val contacts = loadAllContacts()
        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.READ,
                    Telephony.Sms.TYPE
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
                    val displayName = findContactName(address, contacts)
                    messages.add(
                        SmsMessage(
                            id = it.getLong(idCol),
                            address = displayName,
                            body = body,
                            date = it.getLong(dateCol),
                            isRead = it.getInt(readCol) == 1,
                            category = categorize(body, address)
                        )
                    )
                }
            }
        } catch (e: Exception) { }
        return messages
    }

    fun getConversations(): List<Conversation> {
        val messages = getAllSms()
        val grouped = messages.groupBy { it.address }
        return grouped.map { (address, msgs) ->
            val sorted = msgs.sortedByDescending { it.date }
            val unread = msgs.count { !it.isRead }
            Conversation(
                address = address,
                displayName = address,
                lastMessage = sorted.first().body,
                lastDate = sorted.first().date,
                unreadCount = unread,
                category = sorted.first().category,
                messages = sorted
            )
        }.sortedByDescending { it.lastDate }
    }

    private fun categorize(body: String, address: String): SmsCategory {
        val lower = body.lowercase()
        return when {
            lower.contains(Regex("\\b\\d{4,8}\\b")) &&
            (lower.contains("code") || lower.contains("otp") ||
            lower.contains("verify") || lower.contains("uthibitisho") ||
            lower.contains("nambari yako")) -> SmsCategory.OTP

            lower.contains(Regex("(mpesa|tigo|airtel money|benki|bank|balance|tsh|tshs|credited|debited|transfer|imetumwa|umelipa|umelipwa|selcom|songesha)")) ->
            SmsCategory.BANK

            lower.contains(Regex("(winner|won|prize|umeshinda|zawadi|free|promo)")) ->
            SmsCategory.SPAM

            lower.contains(Regex("(offer|sale|discount|shop|punguzo|asilimia|%)")) ->
            SmsCategory.ADS

            else -> SmsCategory.PERSONAL
        }
    }
}
