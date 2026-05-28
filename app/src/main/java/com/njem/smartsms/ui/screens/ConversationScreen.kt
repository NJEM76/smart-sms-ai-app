package com.njem.smartsms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.njem.smartsms.data.model.Conversation
import com.njem.smartsms.data.model.SmsCategory
import com.njem.smartsms.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConversationListScreen(
    conversations: List<Conversation>,
    isLoading: Boolean,
    onConversationClick: (Conversation) -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundDark), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Inapakia mazungumzo...", color = TextSecondary, fontSize = 12.sp)
            }
        }
    } else if (conversations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundDark), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Message, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Hakuna SMS bado", color = TextSecondary, fontSize = 16.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(BackgroundDark),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(conversations) { conversation ->
                ConversationItem(conversation, onConversationClick)
            }
        }
    }
}

@Composable
fun ConversationItem(conversation: Conversation, onClick: (Conversation) -> Unit) {
    val categoryColor = when (conversation.category) {
        SmsCategory.BANK -> BankColor
        SmsCategory.OTP -> OtpColor
        SmsCategory.SPAM -> SpamColor
        SmsCategory.ADS -> AdsColor
        SmsCategory.PERSONAL -> PersonalColor
    }

    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val fullFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val now = System.currentTimeMillis()
    val isToday = now - conversation.lastDate < 86400000
    val timeStr = if (isToday) dateFormat.format(Date(conversation.lastDate))
                  else fullFormat.format(Date(conversation.lastDate))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(conversation) }
            .background(if (conversation.unreadCount > 0) SurfaceDark.copy(alpha = 0.5f) else BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    conversation.displayName.take(1).uppercase(),
                    color = categoryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AccentColor)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(),
                        color = TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    conversation.displayName,
                    color = if (conversation.unreadCount > 0) TextPrimary else TextSecondary,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    timeStr,
                    color = if (conversation.unreadCount > 0) PrimaryColor else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    conversation.lastMessage,
                    color = if (conversation.unreadCount > 0) TextPrimary else TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        when (conversation.category) {
                            SmsCategory.BANK -> "💰"
                            SmsCategory.OTP -> "🔑"
                            SmsCategory.SPAM -> "🚫"
                            SmsCategory.ADS -> "📢"
                            SmsCategory.PERSONAL -> "👤"
                        },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
    Divider(color = SurfaceDark, thickness = 0.5.dp, modifier = Modifier.padding(start = 80.dp))
}
