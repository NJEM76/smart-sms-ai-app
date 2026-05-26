package com.njem.smartsms.data.model

data class SmsMessage(
    val id: Long = 0,
    val address: String = "",
    val body: String = "",
    val date: Long = 0L,
    val isRead: Boolean = false,
    val category: SmsCategory = SmsCategory.PERSONAL
)

enum class SmsCategory {
    PERSONAL,
    BANK,
    OTP,
    ADS,
    SPAM
}
