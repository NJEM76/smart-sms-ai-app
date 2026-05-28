package com.njem.smartsms.data.model

data class Conversation(
    val address: String,
    val displayName: String,
    val lastMessage: String,
    val lastDate: Long,
    val unreadCount: Int,
    val category: SmsCategory,
    val messages: List<SmsMessage>
)
